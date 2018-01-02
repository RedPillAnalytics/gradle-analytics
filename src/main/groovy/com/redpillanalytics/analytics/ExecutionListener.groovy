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
class ExecutionListener implements TaskExecutionListener, BuildListener, ProjectEvaluationListener, StandardOutputListener {

   long startTime
   long endTime

   void beforeExecute(Task task) {

      startTime = System.currentTimeMillis()
   }

   void afterExecute(Task task, TaskState taskState) {

      def buildDir = task.project.rootProject.buildDir
      def basicFields = task.project.rootProject.extensions.analytics.getBasicFields()
      def tasksFile = task.project.rootProject.extensions.analytics.getTasksFile(buildDir)


      endTime = System.currentTimeMillis()

      def ms = (endTime - startTime)

      def gson = new GsonBuilder().serializeNulls().create()

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
                  taskdate   : new Date(startTime).format("yyyy-MM-dd HH:mm:ss"),
                  duration   : ms,
                  status     : taskState.failure ? 'failure' : 'success'
                 ]) + '\n')

      } catch (UnknownDomainObjectException e) {

         log.info "Project '${task.project.name}' is not enabled for Gradle Analytics."
      }

      log.debug "${task.getPath()} took ${ms}ms"

   }

   @Override
   void buildFinished(BuildResult result) {}

   @Override
   void buildStarted(Gradle gradle) {}

   @Override
   void projectsEvaluated(Gradle gradle) {}

   @Override
   void projectsLoaded(Gradle gradle) {}

   @Override
   void settingsEvaluated(Settings settings) {}

   @Override
   void beforeEvaluate(Project project) {}

   @Override
   void afterEvaluate(Project project, ProjectState state) {}

   void onOutput(CharSequence sequence) {}

}
