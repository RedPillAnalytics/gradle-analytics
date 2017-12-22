package com.redpillanalytics.plugin

import com.redpillanalytics.common.CI
import com.redpillanalytics.common.GradleUtils
import com.redpillanalytics.sinks.Sink
import com.redpillanalytics.sinks.records.TestOutput
import com.redpillanalytics.plugin.tasks.FirehoseTask
import com.redpillanalytics.plugin.tasks.GSTask
import com.redpillanalytics.plugin.tasks.JdbcTask
import com.redpillanalytics.plugin.tasks.PubSubTask
import com.redpillanalytics.plugin.tasks.S3Task
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test

@Slf4j
class AnalyticsPlugin implements Plugin<Project> {

   void apply(Project project) {

      // apply Gradle built-in plugins
      project.apply plugin: 'base'

      // apply the Gradle extension plugin and the context container
      applyExtension(project)

      // create configurations
      project.configurations {

         analytics

      }

      project.afterEvaluate {

         // define the method to get build parameters
         def getParameter = { name, defaultValue = null ->

            return GradleUtils.getParameter(project, name, 'analytics')
         }

         def dependencyMatching = { configuration, regexp ->

            return (project.configurations."$configuration".dependencies.find { it.name =~ regexp }) ?: false

         }

         // Logic for determining Build ID
         // first, if we pass a custom buildId, then that's the way to go
         String buildId = getParameter('buildId')

         // easy way to use the buildTag
         if (getParameter('useBuildTag').toBoolean()) {

            project.analytics.buildId = CI.getBuildTag()
         }

         log.debug "buildId: ${buildId}"

         // setup a few reusable parameters for task creation
         String taskName

         project.task('cleanDist', type: Delete) {

            group "Distribution"

            description "Delete the Distributions directory."

            delete project.distsDir
         }

         // define the analytics tests file
         def testsFile = project.analytics.getTestsFile(project.buildDir)

         // Task configuration based on RegressionTestTask Type
         project.tasks.withType(Test).all { Test task ->

            // logging
            testLogging {

               // set options for log level LIFECYCLE
               events "failed"
               exceptionFormat "short"

               // set options for log level DEBUG
               debug.events "started", "skipped", "failed"
               debug.exceptionFormat "full"

               // remove standard output/error logging from --info builds
               // by assigning only 'failed' and 'skipped' events
               info.events = ["failed", "skipped"]
               info.exceptionFormat "short"

            }

            beforeSuite { suite ->

               // is this a new build or not
               if (!testsFile.exists()) {

                  // make the directories
                  testsFile.parentFile.mkdirs()

               }
            }

            afterTest { desc, result ->

               // write tests to the analytics file
               testsFile.append(new Sink(task.project.analytics.ignoreErrors).objectToJson(new com.redpillanalytics.sinks.records.Test(
                       buildid: project.analytics.buildId,
                       organization: project.analytics.organization,
                       hostname: project.analytics.hostname,
                       commithash: CI.commitHash,
                       scmbranch: CI.getBranch(),
                       repositoryurl: CI.getRepositoryUrl(),
                       commitemail: CI.getCommitEmail(),
                       projectdir: project.name,
                       testname: desc.getName(),
                       classname: desc.getClassName(),
                       starttime: new Date(result.getStartTime()).format("yyyy-MM-dd HH:mm:ss"),
                       endtime: new Date(result.getEndTime()).format("yyyy-MM-dd HH:mm:ss"),
                       executecount: result.getTestCount(),
                       successcount: result.getSuccessfulTestCount(),
                       failcount: result.getFailedTestCount(),
                       skipcount: result.getSkippedTestCount()
               )) + '\n')

            }

            onOutput { desc, event ->

               File testOutputFile = project.analytics.getTestOutputFile(project.buildDir)
               testOutputFile.parentFile.mkdirs()

               String className = desc.getClassName().toString()
               String testName = desc.getName().toString()
               String parentName = desc.getParent().toString()

               String type = ((className == testName) ? 'executor' : 'test')

               String eventMessage = event.getMessage().toString()
               String eventDestination = event.getDestination().toString()

               // write tests to the analytics file
               testOutputFile.append(new Sink(task.project.analytics.ignoreErrors).objectToJson(new TestOutput(
                       buildid: project.analytics.buildId,
                       organization: project.analytics.organization,
                       hostname: project.analytics.hostname,
                       commithash: CI.commitHash,
                       scmbranch: CI.getBranch(),
                       repositoryurl: CI.getRepositoryUrl(),
                       commitemail: CI.getCommitEmail(),
                       projectdir: project.name,
                       classname: className,
                       testname: testName,
                       parentname: parentName,
                       processtype: type,
                       destination: eventDestination,
                       message: eventMessage
               )) + '\n')
            }

            afterSuite { desc, result ->
               if (!desc.parent) { // will match the outermost suite
                  log.warn "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
               }
            }

         }

         // Clean Up
         project.task('cleanTemp', type: Delete) {

            description "Delete the task temporary directory."

            delete project.file("${project.buildDir}/tmp")
         }

         // global analytics task
         project.task('produce', type: Zip) {

            group 'analytics'
            description "Analytics workflow task for producing data to all configured sinks."

            if (project.analytics.compress()) {

               from "${project.analytics.getAnalyticsDir(project.buildDir).parent}/"

               appendix 'analytics'
               version CI.getTimestamp()

            }

            doLast {

               if (project.analytics.clean()) {

                  project.delete "${project.analytics.getAnalyticsDir(project.buildDir).parent}"

               }
            }
         }

         // configure analytic groups
         project.analytics.sinks.all { ag ->

            taskName = ag.getTaskName('sink')

            log.debug "analyticGroup name: ${ag.name}"

            // setup Kinesis Firehose functionality
            if (ag.getMechanism() == 'firehose') {

               // Add analytics processing task
               project.task(taskName, type: FirehoseTask) {

                  group 'analytics'

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use S3 API to upload files directly to S3
            if (ag.getMechanism() == 's3') {

               // Add analytics processing task
               project.task(taskName, type: S3Task) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use GS API to upload files directly to GS
            if (ag.getMechanism() == 'gs') {

               // Add analytics processing task
               project.task(taskName, type: GSTask) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // Google PubSub
            if (ag.getMechanism() == 'pubsub') {

               // Add analytics processing task
               project.task(taskName, type: PubSubTask) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use JDBC and built in JSON
            if ((ag.getMechanism() == 'jdbc') && dependencyMatching('analytics', '.*jdbc.*')) {

               // Add analytics processing task
               project.task(taskName, type: JdbcTask) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

                  // connection information
                  username ag.username
                  password ag.password
                  driverUrl ag.driverUrl
                  driverClass ag.driverClass

               }
            }

            if (project.tasks.findByName(taskName)) {

               project.tasks.produce.dependsOn project."${taskName}"
            }


         }
      }

      // end of afterEvaluate

      // add the custom Task Listener that produces Task records
      project.gradle.addListener new DataListener()
   }

   void applyExtension(Project project) {

      // apply the main configuration extension
      project.configure(project) {
         extensions.create('analytics', AnalyticsPluginExtension)
      }

      project.analytics.extensions.sinks = project.container(SinkContainer)

   }
}
