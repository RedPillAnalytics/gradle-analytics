package com.redpillanalytics.sinks

import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import groovy.util.logging.Slf4j

@Slf4j
@groovy.transform.InheritConstructors
class PubSub extends Sink {

   /**
    * Returns the Google Cloud projectId.
    *
    * @return The Google Cloud projectId.
    */
   def getProjectId() {

      def projectId = ServiceOptions.getDefaultProjectId()

      log.debug "project ID: ${projectId}"

      return projectId

   }

   /**
    * Creates a topic, gracefully handling if the topic already exists.
    *
    * @param topicId The topic name to create
    */
   def createTopic(String topicId) {

      try {

         TopicAdminClient topicAdminClient = TopicAdminClient.create()

         ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId)

         topicAdminClient.createTopic(topicName)
      }
      catch (AlreadyExistsException ae) {

         log.debug "Topic $topicId already exists."
      }
      catch (Exception e) {

         if (ignoreErrors) {

            log.info e.toString()

         } else {

            throw e
         }
      }
   }

   /**
    * Publish a message to a Google Cloud PubSub topic.
    *
    * @param topicId The topic name to create
    * @param message The message to send to the topic
    * @return The message future object
    */
   def publishTopic(String topicId, String message) {

      ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId)

      Publisher publisher = Publisher.newBuilder(topicName).build()

      ByteString data = ByteString.copyFromUtf8(message)

      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build()

      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage)

      if (publisher != null) {
         publisher.shutdown()
      }

      return messageIdFuture

   }

   /**
    * Publish te contents of a file to a Google Cloud PubSub topic.
    *
    * @param topicId The topic name to create
    * @param file The file object containing the data to send to the Google Cloud PubSub topic
    * @return The message future object
    */
   def publishTopic(String topicId, File file) {

      def messageIdFuture = publishTopic(topicId, file.text)

      return messageIdFuture

   }
}
