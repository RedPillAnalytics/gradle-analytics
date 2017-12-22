/**
 * Created by stewart on 4/30/15.
 */

package com.redpillanalytics.plugin.tasks

import com.redpillanalytics.sinks.Kinesis
import com.redpillanalytics.common.Utils
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
class FirehoseTask extends SinkTask {

    def getStreamName(File file) {

        return [prefix, Utils.getFileBase(file)].join('.')

    }

    @TaskAction
    def firehoseTask() {

        def kinesis = new Kinesis(project.extensions.analytics.ignoreErrors.toBoolean())

        getAnalyticsDir().eachFile (FileType.DIRECTORIES) { dir ->

            dir.eachFile(FileType.FILES) { file ->

                kinesis.sendRecord(getStreamName(file), file)

            }
        }
    }
}