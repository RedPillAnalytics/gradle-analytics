package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Slf4j
@Title("Execute :tasks")
class TasksTest extends Specification {

   @Shared
   AntBuilder ant = new AntBuilder()

   @Shared
   File projectDir, buildDir, buildFile, settingsFile, artifact

   @Shared
   String taskName

   @Shared
   def result

   @Shared
   String projectName = 'tasks-test'

   def setupSpec() {

      projectDir = new File("${System.getProperty("projectDir")}/$projectName")
      buildDir = new File(projectDir, 'build')

      ant.mkdir(dir: projectDir)

      settingsFile = new File(projectDir, 'settings.gradle').write("""rootProject.name = '$projectName'""")

      buildFile = new File(projectDir, 'build.gradle')

      buildFile.write("""
               |plugins {
               |  id 'com.redpillanalytics.gradle-analytics'
               |}
               |""".stripMargin())
   }

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

   @Unroll
   def "Executing :tasks contains :#task"() {

      given:
      taskName = 'tasks'
      result = executeSingleTask(taskName, ['-Si'])

      expect:
      result.task(":${taskName}").outcome.name() != 'FAILED'
      result.output.contains("$task")

      where:
      task << ['producer']
   }

}
