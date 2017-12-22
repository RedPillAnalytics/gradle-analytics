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
@Title("Execute :tasks")
class TasksTest extends Specification {

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
               pubsub
            }
        """

      result = GradleRunner.create()
              .withProjectDir(testProjectDir.root)
              .withArguments('-Si', 'tasks')
              .withPluginClasspath()
              .build()

      indexedResultOutput = result.output.readLines()

      log.warn result.output
   }

   @Unroll
   def "Executing :tasks contains :#task"() {

      given: "a gradle tasks execution"

      expect:
      result.output.contains("$task")

      where:
      task << ['produce']
   }

}
