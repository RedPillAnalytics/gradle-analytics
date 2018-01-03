package com.redpillanalytics.analytics

import groovy.util.logging.Slf4j
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.tasks.TaskState
import com.google.gson.GsonBuilder

@Slf4j
class AnalyticsListener implements TaskExecutionListener, BuildListener, ProjectEvaluationListener, StandardOutputListener{

   long taskStartTime
   long taskEndTime

   long buildStartTime
   long buildEndTime

   def gson = new GsonBuilder().serializeNulls().create()

   void beforeExecute(Task task) {

      taskStartTime = System.currentTimeMillis()
   }

   void afterExecute(Task task, TaskState taskState) {

      def buildDir = task.project.rootProject.buildDir
      def basicFields = task.project.rootProject.extensions.analytics.getBasicFields()
      def tasksFile = task.project.rootProject.extensions.analytics.getTasksFile(buildDir)

      taskEndTime = System.currentTimeMillis()

      def ms = (taskEndTime - taskStartTime)

      try {
         // define the project JSON file
         tasksFile.parentFile.mkdirs()

         // generate the project JSON file
         tasksFile.append(gson.toJson(basicFields <<
                 [projectname: task.project.project.name,
                  projectdir : task.project.projectDir.path,
                  builddir   : task.project.buildDir.path,
                  taskname   : task.getName(),
                  taskpath   : task.getPath(),
                  taskgroup  : task.getGroup(),
                  taskdesc   : task.getDescription(),
                  taskdate   : new Date(taskStartTime).format("yyyy-MM-dd HH:mm:ss"),
                  duration   : ms,
                  status     : taskState.failure ? 'failure' : 'success',
                  stacktrace : taskState.failure.toString()
                 ]) + '\n')

      } catch (UnknownDomainObjectException e) {

         log.info "Project '${task.project.name}' is not enabled for Gradle Analytics."
      }

      log.debug "${task.getPath()} took ${ms}ms"

   }

   void buildStarted(Gradle gradle) {}

   void buildFinished(BuildResult result) {

      buildEndTime = System.currentTimeMillis()

      def ms = (buildEndTime - buildStartTime)

      File buildsFile = result.gradle.rootProject.extensions.analytics.getBuildsFile(result.gradle.rootProject.buildDir)

      buildsFile.parentFile.mkdirs()

      // generate the project JSON file
      buildsFile.append(gson.toJson(result.gradle.rootProject.extensions.analytics.getBasicFields() <<
              [hostname       : result.gradle.rootProject.project.extensions.analytics.hostname,
               commithash     : result.gradle.rootProject.project.extensions.analytics.gitCommitHash,
               scmbranch      : result.gradle.rootProject.project.extensions.analytics.gitBranch,
               repositoryurl  : result.gradle.rootProject.project.extensions.analytics.gitRepositoryUrl,
               commitemail    : result.gradle.rootProject.project.extensions.analytics.gitCommitEmail,
               rootprojectname: result.gradle.rootProject.project.name,
               rootprojectdir : result.gradle.rootProject.projectDir.path,
               rootbuilddir   : result.gradle.rootProject.buildDir.path,
               builddate      : new Date(buildStartTime).format("yyyy-MM-dd HH:mm:ss"),
               duration       : ms,
               status         : result.failure ? 'failure' : 'success',
               stacktrace     : result.failure.toString()
              ]) + '\n')
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
