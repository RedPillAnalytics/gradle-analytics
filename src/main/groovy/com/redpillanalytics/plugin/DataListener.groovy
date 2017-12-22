package com.redpillanalytics.plugin

import com.redpillanalytics.common.CI
import com.redpillanalytics.sinks.Sink
import groovy.util.logging.Slf4j
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.tasks.TaskState


/**
 * Created by stewart on 3/19/16.
 */

@Slf4j
class DataListener implements TaskExecutionListener, BuildListener, ProjectEvaluationListener, StandardOutputListener {

    long startTime
    long endTime

    void beforeExecute(Task task) {

        startTime = System.currentTimeMillis()
    }

    void afterExecute(Task task, TaskState taskState) {

        endTime = System.currentTimeMillis()

        def ms = (endTime - startTime)


        try {
            // define the project JSON file
            def tasksFile = task.project.extensions.analytics.getTasksFile(task.project.buildDir)
            tasksFile.parentFile.mkdirs()

            // generate the project JSON file
            tasksFile.append(new Sink(task.project.extensions.analytics.ignoreStreamErrors).objectToJson(new com.redpillanalytics.sinks.records.Task(
                    buildid: task.project.extensions.analytics.buildId,
                    organization: task.project.extensions.analytics.organization,
                    hostname: task.project.extensions.analytics.hostname,
                    commithash: CI.commitHash,
                    scmbranch: CI.getBranch(),
                    repositoryurl: CI.getRepositoryUrl(),
                    commitemail: CI.getCommitEmail(),
                    projectdir: task.project.name,
                    builddir: task.project.buildDir,
                    taskname: task.getName(),
                    taskpath: task.getPath(),
                    taskgroup: task.getGroup(),
                    taskdesc: task.getDescription(),
                    taskdate: new Date(startTime).format("yyyy-MM-dd HH:mm:ss"),
                    duration: ms,
                    status: taskState.failure ? 'failure' : 'success'
            )) + '\n')

        } catch (UnknownDomainObjectException e) {

            log.info "Project '${task.project.name}' not enabled for Analytics."
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
    void beforeEvaluate(org.gradle.api.Project project) {}

    @Override
    void afterEvaluate(org.gradle.api.Project project, ProjectState state) {}

    void onOutput(CharSequence sequence) {}

}
