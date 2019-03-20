package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.analytics.sinks.PubSub
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class PubSubTask extends SinkTask {

   @TaskAction
   def pubSubTask() {

      def pubsub = new PubSub(ignoreErrors)

      analyticsFiles.each{ file ->
         def topicName = getEntityName(file)
         // first create the PubSub topic if it doesn't already exist
         pubsub.createTopic(topicName)
         // Now publish the message
         def messageIdFuture = pubsub.publishTopic(topicName, file)
         return messageIdFuture
      }
      logSink()
   }
}