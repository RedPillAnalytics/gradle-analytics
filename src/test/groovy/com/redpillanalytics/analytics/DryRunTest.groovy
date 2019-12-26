package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Slf4j
@Title("Execute :publish task using --dry-run")
class DryRunTest extends Specification {

   @ClassRule
   @Shared
   TemporaryFolder testProjectDir = new TemporaryFolder()

   @Shared buildFile
   @Shared result
   @Shared indexedResultOutput

   // run the Gradle build
   // return regular output
   def setupSpec() {

      buildFile = testProjectDir.newFile('build.gradle').write("""
               |plugins {
               |  id 'com.redpillanalytics.gradle-analytics'
               |}
               |analytics {
               |  s3 {test}
               |  kafka {test}
               |  firehose {test}
               |  gcs {test}
               |  kafka {test}
               |}
               |""".stripMargin())

      result = GradleRunner.create()
              .withProjectDir(testProjectDir.root)
              .withArguments('-Sim', 'producer')
              .withPluginClasspath()
              .build()

      indexedResultOutput = result.output.readLines()

      log.warn result.output
   }

   @Unroll
   def "a dry run configuration contains :#task"() {

      given: "a dry run task"

      expect:
      result.output.contains(":$task")

      where:
      task << ['producer','firehoseTestSink','gcsTestSink','kafkaTestSink','s3TestSink']
   }

   @Unroll
   def "a dry run configuration ensures :#firstTask runs before :#secondTask"() {

      given: "a dry-run build executing :producer"

      expect:
      indexedResultOutput.findIndexOf { it =~ /(:$firstTask)( SKIPPED)/ } < indexedResultOutput.findIndexOf {
         it =~ /(:$secondTask)( SKIPPED)/
      }

      where:

      firstTask << ['s3TestSink','kafkaTestSink','gcsTestSink','firehoseTestSink']
      secondTask << ['producer', 'producer','producer','producer']
   }

}
