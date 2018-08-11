package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Slf4j
@Title("Execute :publish task using --dry-run")
class AvroProducerTest extends Specification {

   @ClassRule
   @Shared
   TemporaryFolder testProjectDir = new TemporaryFolder()

   @Shared
           buildFile
   @Shared
           result
   @Shared
           indexedResultOutput

   // run the Gradle build
   // return regular output
   def setupSpec() {

      buildFile = testProjectDir.newFile('build.gradle')
      buildFile << """
            plugins {
                id 'com.redpillanalytics.gradle-analytics'
            }
            
            analytics.sinks {
               kafka {
                  registry = 'http://localhost:8081'
               }
            }
            
            analytics {
               ignoreErrors = false
               format = 'Avro'
            } 
        """

      result = GradleRunner.create()
              .withProjectDir(testProjectDir.root)
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
