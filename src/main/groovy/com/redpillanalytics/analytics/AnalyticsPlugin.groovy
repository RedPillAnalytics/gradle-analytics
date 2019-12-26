package com.redpillanalytics.analytics

import com.redpillanalytics.analytics.containers.FirehoseContainer
import com.redpillanalytics.analytics.containers.KafkaContainer
import com.redpillanalytics.analytics.containers.S3Container
import com.redpillanalytics.analytics.containers.SinkContainer
import com.redpillanalytics.analytics.tasks.FirehoseTask
import com.redpillanalytics.analytics.tasks.GSTask
import com.redpillanalytics.analytics.tasks.JdbcTask
import com.redpillanalytics.analytics.tasks.KafkaTask
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

      // apply plugin for git properties
      project.apply plugin: "com.redpillanalytics.gradle-properties"

      // apply plugin for git properties
      project.apply plugin: "org.ajoberstar.grgit"
      project.apply plugin: "org.dvaske.gradle.git-build-info"

      // create git extensions
      project.ext.gitDescribeInfo = project.grgit?.describe(longDescr: true, tags: true)
      project.ext.gitLastTag = (project.ext.gitDescribeInfo?.split('-')?.getAt(0)) ?: 'v0.1.0'
      project.ext.gitLastVersion = project.ext.gitLastTag.replaceAll(/(^\w)/, '')

      // apply the Gradle extension plugin and the context container
      applyExtension(project)

      // create configurations
      project.configurations {
         analytics
      }

      project.afterEvaluate {

         project.extensions.pluginProps.setParameters(project, 'analytics')
         // create git extensions
         // setup a few reusable parameters for task creation
         String taskName

         project.task('cleanDist', type: Delete) {
            group "Distribution"
            description "Delete the Distributions directory."
            delete project.distsDir
         }

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

               afterTest { desc, result ->

                  // write tests to the analytics file
                  project.extensions.analytics.writeAnalytics(

                          project.rootProject.extensions.analytics.testsFileName as String,
                          project.rootProject.buildDir,
                          project.rootProject.extensions.analytics.getBuildHeader() <<
                                  [
                                          projectname : task.project.displayName,
                                          projectdir  : task.project.projectDir.path,
                                          builddir    : task.project.buildDir.path,
                                          buildfile   : task.project.buildFile.path,
                                          testname    : desc.getName(),
                                          classname   : desc.getClassName(),
                                          starttime   : new Date(result.getStartTime()).format("yyyy-MM-dd HH:mm:ss"),
                                          endtime     : new Date(result.getEndTime()).format("yyyy-MM-dd HH:mm:ss"),
                                          executecount: result.getTestCount(),
                                          successcount: result.getSuccessfulTestCount(),
                                          failcount   : result.getFailedTestCount(),
                                          skipcount   : result.getSkippedTestCount()
                                  ],
                  )
               }

               onOutput { desc, event ->

                  String className = desc.getClassName().toString()
                  String testName = desc.getName().toString()
                  String parentName = desc.getParent().toString()

                  String type = ((className == testName) ? 'executor' : 'test')

                  String eventMessage = event.getMessage().toString()
                  String eventDestination = event.getDestination().toString()

                  // write tests to the analytics file
                  if (eventMessage != '\n') {

                     project.extensions.analytics.writeAnalytics(

                             project.rootProject.extensions.analytics.testOutputFileName as String,
                             project.rootProject.buildDir,
                             project.rootProject.extensions.analytics.getBuildHeader() <<
                                     [
                                             projectname: task.project.displayName,
                                             projectdir : task.project.projectDir.path,
                                             builddir   : task.project.buildDir.path,
                                             buildfile  : task.project.buildFile.path,
                                             classname  : className,
                                             testname   : testName,
                                             parentname : parentName,
                                             processtype: type,
                                             destination: eventDestination,
                                             message    : eventMessage
                                     ],
                     )

                  }
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

         // configure Kafka sink
         project.analytics.kafka.all { sink ->

            // Add analytics processing task
            project.task(sink.getTaskName(), type: KafkaTask) {
               group "analytics"
               description sink.getDescription()

               // add standard properties
               prefix sink.getPrefix()
               joiner sink.getJoiner()
               suffix(sink.getFormatSuffix() ? project.extensions.analytics.format : null)

               // custom Kafka properties
               bootstrapServers = sink.getBootstrapServers() ?: 'localhost:9092'
               serializerKey = sink.getSerializerKey() ?: "org.apache.kafka.common.serialization.StringSerializer"
               serializerValue = sink.getSerializerValue() ?: "org.apache.kafka.common.serialization.StringSerializer"
               acks sink.getAcks() ?: 'all'

               // confluent schema registry
               schemaRegistry sink.getSchemaRegistry() ?: null
            }
            project.producer.dependsOn sink.getTaskName()
         }

         // configure Firehose sink
         project.analytics.firehose.all { sink ->

            // Add analytics processing task
            project.task(sink.getTaskName(), type: FirehoseTask) {
               group 'analytics'
               description sink.getDescription()
               // add any custom prefix to sink names
               prefix sink.getPrefix()
               joiner sink.getJoiner()
               // handle the suffix
               suffix(sink.getFormatSuffix() ? project.extensions.analytics.format : null)
            }
            project.producer.dependsOn sink.getTaskName()
         }

         // configure Firehose sink
         project.analytics.s3.all { sink ->

            // Add analytics processing task
            project.task(sink.getTaskName(), type: S3Task) {
               group 'analytics'
               description sink.getDescription()
               // add any custom prefix to sink names
               prefix sink.getPrefix()
               joiner sink.getJoiner()
               suffix(sink.getFormatSuffix() ? project.extensions.analytics.format : null)
            }
            project.producer.dependsOn sink.getTaskName()
         }

         // configure analytic groups
//         project.analytics.sinks.all { ag ->
//
//
//
//            // use S3 API to upload files directly to S3
//            if (ag.getSink() == 's3') {
//
//               // Add analytics processing task
//               project.task(taskName, type: S3Task) {
//                  group "analytics"
//                  description ag.getDescription()
//                  // add any custom prefix to sink names
//                  prefix ag.getPrefix()
//               }
//            }
//
//            // use GS API to upload files directly to GS
//            if (ag.getSink() == 'gs') {
//
//               // Add analytics processing task
//               project.task(taskName, type: GSTask) {
//
//                  group "analytics"
//                  description ag.getDescription()
//                  // add any custom prefix to sink names
//                  prefix ag.getPrefix()
//               }
//            }
//
//            // Google PubSub
//            if (ag.getSink() == 'pubsub') {
//
//               // Add analytics processing task
//               project.task(taskName, type: PubSubTask) {
//
//                  group "analytics"
//                  description ag.getDescription()
//                  // add any custom prefix to sink names
//                  prefix ag.getPrefix()
//
//               }
//
//            }
//
//
//            // use JDBC and built in JSON
//            if ((ag.getSink() == 'jdbc') && project.extensions.pluginProps.dependencyMatching('analytics', '.*jdbc.*')) {
//
//               // Add analytics processing task
//               project.task(taskName, type: JdbcTask) {
//
//                  group "analytics"
//
//                  description ag.getDescription()
//
//                  // add any custom prefix to sink names
//                  prefix ag.getPrefix()
//
//                  // connection information
//                  username ag.username
//                  password ag.password
//                  driverUrl ag.driverUrl
//                  driverClass ag.driverClass
//               }
//            }
//         }
      }

      // end of afterEvaluate
   }

   void applyExtension(Project project) {

      if (project == project.rootProject) {
         project.configure(project) {
            extensions.create('analytics', AnalyticsPluginExtension)
         }
         project.analytics.extensions.kafka = project.container(KafkaContainer)
         project.analytics.extensions.firehose = project.container(FirehoseContainer)
         project.analytics.extensions.s3 = project.container(S3Container)
         project.gradle.addListener new AnalyticsListener()

      } else {
         throw new GradleException("Gradle Analytics may only be applied to the root project.")
      }
   }

}

