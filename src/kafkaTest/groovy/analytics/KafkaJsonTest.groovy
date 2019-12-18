package analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

@Slf4j
@Testcontainers
@Title("Execute :publish task using --dry-run")
class KafkaJsonTest extends Specification {

   @Shared
   File projectDir, buildDir, buildFile, settingsFile, resourcesDir
   @Shared
   AntBuilder ant = new AntBuilder()
   @Shared
   String projectName = 'json-test', taskName

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
            |    id 'com.redpillanalytics.gradle-analytics'
            |}
            |
            |analytics {
            |   ignoreErrors = false
            |   sinks {
            |      kafka {
            |        servers = '${kafka.getBootstrapServers()}'
            |      }
            |   }
            |}
            |""".stripMargin()
      )
   }

   // helper method
   def executeSingleTask(String taskName, List otherArgs, Boolean logOutput = true) {

      otherArgs.add(0, taskName)

      log.warn "runner arguments: ${otherArgs.toString()}"

      // execute the Gradle test build
      result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withArguments(otherArgs)
              .withPluginClasspath()
              .build()

      // log the results
      if (logOutput) log.warn result.getOutput()

      return result

   }

   def "Execute :tasks task"() {
      given:
      taskName = 'tasks'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      result.task(":${taskName}").outcome.name() != 'FAILED'
   }

   def "Execute :build task"() {
      given:
      taskName = 'build'
      result = executeSingleTask(taskName, ['-Si',"-Panalytics.organization=Red Pill Analytics"])

      expect:
      result.task(":${taskName}").outcome.name() != 'FAILED'
   }

   def "Execute :producer task"() {
      given:
      taskName = 'producer'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      result.task(":${taskName}").outcome.name() != 'FAILED'
   }

}
