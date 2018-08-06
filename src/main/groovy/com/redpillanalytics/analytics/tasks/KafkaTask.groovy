package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.sinks.Kafka
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class KafkaTask extends SinkTask {

   @TaskAction
   def kafkaTask() {

      def sink = new Kafka(ignoreErrors)

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def topicName = getEntityName(file)

            // Now publish the message
            def response = sink.publishTopic(topicName, file)

            return response

         }
      }
   }
}