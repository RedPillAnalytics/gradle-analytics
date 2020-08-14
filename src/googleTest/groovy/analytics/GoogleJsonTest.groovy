package analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Title

@Slf4j
@Stepwise
@Title("Execute :publish task using --dry-run")
class GoogleJsonTest extends Specification {

   @Shared
   File projectDir, buildDir, buildFile, settingsFile
   @Shared
   AntBuilder ant = new AntBuilder()
   @Shared
   String projectName = 'json-test', taskName

   @Shared
   def result

   // run the Gradle build
   // return regular output
   def setupSpec() {

      projectDir = new File("${System.getProperty("projectDir")}/$projectName")
      ant.mkdir(dir: projectDir)
      buildDir = new File(projectDir, 'build')

      settingsFile = new File(projectDir, 'settings.gradle').write("""rootProject.name = '$projectName'""")

      buildFile = new File(projectDir, 'build.gradle').write("""
            |plugins {
            |    id 'com.redpillanalytics.gradle-analytics'
            |}
            |
            |analytics {
            |  ignoreErrors = false
            |  gcs {
            |     test {
            |        bucket = 'rpa-gradle-analytics'
            |     }
            |  }
            |  bq {
            |     test {
            |        bucket = 'rpa-gradle-analytics-bq'
            |        dataset = 'gradle_analytics_test'
            |     }
            |  }
            |}
            |""".stripMargin()
      )
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

   @Ignore
   def "Execute :pubsubTestSink task"() {
      given:
      taskName = 'pubsubTestSink'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :gcsTestSink task"() {
      given:
      taskName = 'gcsTestSink'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :bqTestSink task"() {
      given:
      taskName = 'bqTestSink'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }

   def "Execute :producer task"() {
      given:
      taskName = 'producer'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      !result.tasks.collect { it.outcome }.contains('FAILURE')
   }
}
