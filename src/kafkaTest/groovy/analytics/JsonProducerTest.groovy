package analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Slf4j
@Title("Execute :publish task using --dry-run")
class JsonProducerTest extends Specification {

   @Shared
   File projectDir, buildDir, buildFile, settingsFile, resourcesDir

   @Shared
   AntBuilder ant = new AntBuilder()

   @Shared
   String projectName = 'json-test'

   @Shared
   def result
   @Shared
   def indexedResultOutput

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
            |   compressFiles = false
            |   cleanFiles = false
            |   sinks {
            |      kafka
            |   }
            |}
            |""".stripMargin()
      )

      result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withArguments('-Si', 'build', 'producer')
              .withPluginClasspath()
              .build()

      indexedResultOutput = result.output.readLines()

      log.warn result.output
   }

   @Unroll
   def "build was successful"() {

      given: "an integration test execution"

      expect:
      result.output.contains("BUILD SUCCESSFUL")

   }
}
