/**
 * Created by stewart on 4/30/15.
 */

package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.analytics.sinks.Kinesis
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
class FirehoseTask extends SinkTask {

   @TaskAction
   def firehoseTask() {

      def kinesis = new Kinesis(project.extensions.analytics.ignoreErrors.toBoolean())

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            kinesis.sendRecord(getEntityName(file), file)

         }
      }
   }
}