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

      buildFile = new File(projectDir, 'build.gradle').write("""
               |plugins {
               |   id 'com.redpillanalytics.gradle-analytics'
               |}
               |
               |analytics {
               |   ignoreErrors = false
               |   kafka {
               |      prod {
               |         bootstrapServers = '${kafka.getBootstrapServers()}'
               |         schemaRegistry = 'http://192.168.1.35:8081'
               |         acks = 'all'
               |      }
               |   }
               |   s3 {
               |      dev {
               |         prefix = 'rpa-gradle'
               |      }
               |   }
               |   gcs {
               |      prod {
               |         prefix = 'rpa-gradle'
               |      }     
               |   }
               |}
               |""".stripMargin())
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

   def "Execute :tasks task"() {
      given:
      taskName = 'tasks'
      result = executeSingleTask(taskName, ['-Si'])

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
