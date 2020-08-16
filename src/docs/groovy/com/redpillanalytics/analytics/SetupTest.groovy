package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Title

@Slf4j
@Stepwise
@Testcontainers
@Title("Execute :publish task using --dry-run")
class SetupTest extends Specification {

   @Shared
   File projectDir, buildDir, buildFile, settingsFile
   @Shared
   AntBuilder ant = new AntBuilder()
   @Shared
   String projectName = 'setup-test', taskName

   @Shared
   def result

   @Shared
   KafkaContainer kafka = new KafkaContainer()

   // run the Gradle build
   // return regular output
   def setupSpec() {

      projectDir = new File("${System.getProperty("projectDir")}/$projectName")
      ant.mkdir(dir: projectDir)
      buildDir = new File(projectDir, 'build')

      settingsFile = new File(projectDir, 'settings.gradle').write("""rootProject.name = '$projectName'""")

      buildFile = new File(projectDir, 'build.gradle')

      buildFile.write("""
               |plugins {
               |   id 'com.redpillanalytics.gradle-analytics'
               |}
               |
               |analytics {
               |   ignoreErrors = false
               |
               |   // write to Google BigQuery
               |   bq {
               |      // can configure multiple locations or "environments"
               |      // used in generating the task name
               |      test {
               |         // Files are first staged in a GCS bucket
               |         bucket = 'rpa-gradle'
               |         // then they are loaded to a BigQuery dataset
               |         dataset = 'gradle_analytics'
               |      }     
               |   }
               |
               |   // write to a Kafka cluster
               |   kafka {
               |      prod {
               |         bootstrapServers = '${kafka.getBootstrapServers()}'
               |         acks = 'all'
               |      }
               |   }
               |   // write to an S3 bucket
               |   s3 {
               |      dev {
               |         // the bucket name to write to
               |         bucket = 'rpa-gradle'
               |        // a suffix to add to the end of the default entity names
               |        suffix = 'dev'
               |      }
               |   }
               |   // write to a Google Cloud Storage bucket
               |   gcs {
               |      prod {
               |         // the bucket name to write to
               |         bucket = 'rpa-gradle'
               |         // a prefix to add to the beginning of the default entity names
               |         prefix = 'prod'
               |      }     
               |   }
               |}
               |""".stripMargin())

      log.warn buildFile.text
   }

   // helper method
   def executeSingleTask(String taskName, List otherArgs = []) {

      otherArgs.add(0, taskName)

      log.warn "runner arguments: ${otherArgs.toString()}"

      // execute the Gradle test build
      result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withArguments(otherArgs)
              .withPluginClasspath()
              .forwardOutput()
              .build()
   }

   def "Execute :tasks task --group analytics"() {
      given:
      taskName = 'tasks'
      result = executeSingleTask(taskName, ['--group', 'analytics', '-S'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :build task"() {
      given:
      taskName = 'build'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :producer dryrun task"() {
      given:
      taskName = 'producer'
      result = executeSingleTask(taskName, ['-Sm'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :producer task"() {
      given:
      taskName = 'producer'
      result = executeSingleTask(taskName, ['-S'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }
}
