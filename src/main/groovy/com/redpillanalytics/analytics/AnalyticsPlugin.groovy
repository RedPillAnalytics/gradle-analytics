package com.redpillanalytics.analytics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.redpillanalytics.analytics.tasks.FirehoseTask
import com.redpillanalytics.analytics.tasks.GSTask
import com.redpillanalytics.analytics.tasks.JdbcTask
import com.redpillanalytics.analytics.tasks.PubSubTask
import com.redpillanalytics.analytics.tasks.S3Task
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
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

         // Go look for any -P properties that have "analytics." in them
         // If so... update the extension value
         project.ext.properties.each { key, value ->

            //log.debug "extension: " + key + ' | ' + value

            if (key =~ /analytics\./) {

               def list = key.toString().split(/\./)

               def extension = list[0]
               def property = list[1]

               if (extension == 'analytics' && project.analytics.hasProperty(property)) {

                  log.debug "Setting configuration property for extension: $extension, property: $property, value: $value"

                  if (project.extensions.getByName(extension)."$property" instanceof Boolean) {

                     project.extensions.getByName(extension)."$property" = value.toBoolean()
                  } else if (project.extensions.getByName(extension)."$property" instanceof Integer) {

                     project.extensions.getByName(extension)."$property" = value.toInteger()
                  } else {

                     project.extensions.getByName(extension)."$property" = value
                  }
               }
            }
         }

         def dependencyMatching = { configuration, regexp ->

            return (project.configurations."$configuration".dependencies.find { it.name =~ regexp }) ?: false

         }

         // Get a Gson object
         Gson gson = new GsonBuilder().serializeNulls().create()

         // setup a few reusable parameters for task creation
         String taskName

         project.task('cleanDist', type: Delete) {

            group "Distribution"

            description "Delete the Distributions directory."

            delete project.distsDir
         }

         // define the analytics tests file
         File testsFile = project.extensions.analytics.getTestsFile(project.buildDir)
         File testOutputFile = project.extensions.analytics.getTestOutputFile(project.buildDir)
         def basicFields = project.rootProject.extensions.analytics.getBasicFields()


         // Task configuration based on Test task type

         project.rootProject.getAllprojects().each { Project proj ->

            proj.tasks.withType(Test).all { Test task ->

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
                  testsFile.append(gson.toJson(basicFields <<
                          [projectname : task.project.project.name,
                           projectdir  : task.project.projectDir.path,
                           builddir    : task.project.buildDir.path,
                           testname    : desc.getName(),
                           classname   : desc.getClassName(),
                           starttime   : new Date(result.getStartTime()).format("yyyy-MM-dd HH:mm:ss"),
                           endtime     : new Date(result.getEndTime()).format("yyyy-MM-dd HH:mm:ss"),
                           executecount: result.getTestCount(),
                           successcount: result.getSuccessfulTestCount(),
                           failcount   : result.getFailedTestCount(),
                           skipcount   : result.getSkippedTestCount()
                          ]) + '\n')
               }

               onOutput { desc, event ->

                  testOutputFile.parentFile.mkdirs()

                  String className = desc.getClassName().toString()
                  String testName = desc.getName().toString()
                  String parentName = desc.getParent().toString()

                  String type = ((className == testName) ? 'executor' : 'test')

                  String eventMessage = event.getMessage().toString()
                  String eventDestination = event.getDestination().toString()

                  // write tests to the analytics file
                  testOutputFile.append(gson.toJson(basicFields <<
                          [projectname: task.project.project.name,
                           projectdir : task.project.projectDir.path,
                           builddir   : task.project.buildDir.path,
                           classname  : className,
                           testname   : testName,
                           parentname : parentName,
                           processtype: type,
                           destination: eventDestination,
                           message    : eventMessage
                          ]) + '\n')
               }

               afterSuite { desc, result ->
                  if (!desc.parent) { // will match the outermost suite
                     log.warn "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
                  }
               }

            }
         }

         // global analytics task
         project.task('producer', type: Zip) {

            group 'analytics'
            description "Analytics workflow task for producing data to all configured sinks."

            mustRunAfter project.build

            if (project.analytics.compressFiles) {

               log.debug "Analytics files will be compressed after production."

               from "${project.analytics.getAnalyticsDir(project.buildDir).parent}/"

               baseName 'analytics'
               appendix project.extensions.analytics.buildId

            }

            doLast {

               if (project.analytics.cleanFiles) {

                  log.debug "Analytics files will be deleted after production."

                  project.delete "${project.analytics.getAnalyticsDir(project.buildDir).parent}"

               }
            }
         }

         // configure analytic groups
         project.analytics.sinks.all { ag ->

            taskName = ag.getTaskName('sink')

            log.debug "analyticGroup name: ${ag.name}"

            // setup Kinesis Firehose functionality
            if (ag.getSink() == 'firehose') {

               // Add analytics processing task
               project.task(taskName, type: FirehoseTask) {

                  group 'analytics'

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use S3 API to upload files directly to S3
            if (ag.getSink() == 's3') {

               // Add analytics processing task
               project.task(taskName, type: S3Task) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use GS API to upload files directly to GS
            if (ag.getSink() == 'gs') {

               // Add analytics processing task
               project.task(taskName, type: GSTask) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // Google PubSub
            if (ag.getSink() == 'pubsub') {

               // Add analytics processing task
               project.task(taskName, type: PubSubTask) {

                  group "analytics"

                  description ag.getDescription()

                  // add any custom prefix to sink names
                  prefix ag.getPrefix()

               }

            }

            // use JDBC and built in JSON
            if ((ag.getSink() == 'jdbc') && dependencyMatching('analytics', '.*jdbc.*')) {

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

               project.tasks.producer.dependsOn project."${taskName}"
            }


         }
      }

      // end of afterEvaluate
   }

   void applyExtension(Project project) {

      if (project == project.rootProject) {

         project.configure(project) {
            extensions.create('analytics', AnalyticsPluginExtension)
         }

         project.analytics.extensions.sinks = project.container(SinkContainer)

         project.gradle.addListener new ExecutionListener()

      } else {
         throw GradleException("Gradle Analytics may only be applied to the root project.")
      }

   }
}
