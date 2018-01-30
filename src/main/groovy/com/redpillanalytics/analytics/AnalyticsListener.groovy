package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.tasks.TaskState

@Slf4j
class AnalyticsListener implements TaskExecutionListener, BuildListener, ProjectEvaluationListener, StandardOutputListener {

   long taskStartTime
   long taskEndTime

   long buildStartTime
   long buildEndTime

   void beforeExecute(Task task) {

      taskStartTime = System.currentTimeMillis()
   }

   void afterExecute(Task task, TaskState taskState) {

      taskEndTime = System.currentTimeMillis()

      def ms = (taskEndTime - taskStartTime)

      // write tests to the analytics file
      task.project.rootProject.extensions.analytics.writeAnalytics(

              task.project.rootProject.extensions.analytics.tasksFileName as String,
              task.project.rootProject.buildDir,
              [
                      projectname: task.project.displayName,
                      projectdir : task.project.projectDir.path,
                      builddir   : task.project.buildDir.path,
                      buildfile  : task.project.buildFile.path,
                      taskname   : task.getName(),
                      taskpath   : task.getPath(),
                      taskgroup  : task.getGroup(),
                      taskdesc   : task.getDescription(),
                      taskdate   : new Date(taskStartTime).format("yyyy-MM-dd HH:mm:ss"),
                      didWork    : task.didWork.toString(),
                      duration   : ms,
                      status     : taskState.failure ? 'failure' : 'success',
                      stacktrace : taskState.failure.toString()
              ],
              true
      )

      log.debug "${task.getPath()} took ${ms}ms"

   }

   void buildStarted(Gradle gradle) {}

   void buildFinished(BuildResult result) {

      buildEndTime = System.currentTimeMillis()

      def ms = (buildEndTime - buildStartTime)

      result.gradle.rootProject.extensions.analytics.writeAnalytics(

              result.gradle.rootProject.extensions.analytics.buildsFileName as String,
              result.gradle.rootProject.buildDir,
              [
                      hostname       : result.gradle.rootProject.project.extensions.analytics.hostname,
                      commithash     : result.gradle.rootProject.project.extensions.analytics.gitCommitHash,
                      scmbranch      : result.gradle.rootProject.project.extensions.analytics.gitBranch,
                      repositoryurl  : result.gradle.rootProject.project.extensions.analytics.gitRepositoryUrl,
                      commitemail    : result.gradle.rootProject.project.extensions.analytics.gitCommitEmail,
                      rootprojectname: result.gradle.rootProject.project.name,
                      rootprojectdir : result.gradle.rootProject.projectDir.path,
                      rootbuilddir   : result.gradle.rootProject.buildDir.path,
                      version        : result.gradle.rootProject.version,
                      builddate      : new Date(buildStartTime).format("yyyy-MM-dd HH:mm:ss"),
                      duration       : ms,
                      status         : result.failure ? 'failure' : 'success',
                      stacktrace     : result.failure.toString()
              ],
              true
      )
   }

   void projectsEvaluated(Gradle gradle) {}

   void projectsLoaded(Gradle gradle) {}

   void settingsEvaluated(Settings settings) {}

   void beforeEvaluate(Project project) {}

   void afterEvaluate(Project project, ProjectState state) {

      if (project == project.rootProject) {

         buildStartTime = System.currentTimeMillis()
      }
   }

   void onOutput(CharSequence sequence) {}

}
