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
class PropertiesTest extends Specification {

   @ClassRule
   @Shared
   TemporaryFolder testProjectDir = new TemporaryFolder()

   @Shared
   File buildFile
   @Shared
   def result

   // run the Gradle build
   // return regular output
   def setupSpec() {

      buildFile = testProjectDir.newFile('build.gradle')
      buildFile << """
            plugins {
                id 'com.redpillanalytics.gradle-analytics'
            }
        """

      result = GradleRunner.create()
              .withProjectDir(testProjectDir.root)
              .withArguments('properties')
              .withPluginClasspath()
              .build()

      log.warn result.output
   }


   @Unroll
   def "properties contains #property"() {

      given: "executing Gradle :properties"

      expect:
      result.output.contains("$property:")

      where:
      property << ['gitDescribeInfo', 'gitLastTag', 'gitLastVersion', 'gitBranch', 'ci']
   }
}
