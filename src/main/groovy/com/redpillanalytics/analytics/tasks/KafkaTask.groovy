package com.redpillanalytics.analytics.tasks


import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class KafkaTask extends SinkTask {

   @Input
   String servers

   @Input
   @Optional
   String serializerKey

   @Input
   @Optional
   String serializerValue

   @Input
   @Optional
   String acks

   @TaskAction
   def kafkaTask() {

      def producer = new KafkaProducer([
              "bootstrap.servers": servers,
              // serializers
              "value.serializer" : serializerValue,
              "key.serializer"   : serializerKey,
              // acknowledgement control
              "acks"             : acks
      ])

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def topicName = getEntityName(file)

            def response

            try {

               response = producer.send(new ProducerRecord(topicName, file.text))
            }
            catch (Exception e) {

               if (ignoreErrors) {

                  logger.info e.toString()

               } else {

                  throw e
               }
            }
            finally {

               logger.warn "response: $response"

            }

            return response
         }
      }
   }
}