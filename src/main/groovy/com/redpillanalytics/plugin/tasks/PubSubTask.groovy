/**
 * Created by stewart on 4/30/15.
 */

package com.redpillanalytics.plugin.tasks

import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import com.redpillanalytics.common.Utils
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class PubSubTask extends SinkTask {

   @Input
   String getTopicName(File file) {

      def topicName = [prefix, Utils.getFileBase(file)].join('.')
      log.debug "Name of the topic: $topicName"
      return topicName

   }

   @Input
   def getProjectId(){

      return ServiceOptions.getDefaultProjectId()

   }

   def createTopic(String topicId) {

      try {
         log.debug "project ID: ${projectId.dump()}"

         TopicName topic = TopicName.create(projectId, topicId)

         TopicAdminClient topicAdminClient = TopicAdminClient.create()

         topicAdminClient.createTopic(topic)
      }
      catch (AlreadyExistsException ae) {

         log.debug "Topic $topicId already exists."
      }
      catch (Exception e) {

         if (project.analytics.ignoreErrors.toBoolean()) {

            log.info e.toString()

         } else {

            throw e
         }
      }
   }

   @TaskAction
   def pubSubTask() {

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def topicName = getTopicName(file)

            // first create the PubSub topic if it doesn't already exist

            createTopic(topicName)

            //pubsub.sendMessage(topicName, file)

            // pasting in code
            TopicName topic = TopicName.create(projectId, topicName)

            Publisher publisher = null

            publisher = Publisher.defaultBuilder(topic).build()

            ByteString data = ByteString.copyFromUtf8(file.text)

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build()

            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage)

            if (publisher != null) {
               publisher.shutdown()
            }

            return messageIdFuture

         }
      }
   }
}