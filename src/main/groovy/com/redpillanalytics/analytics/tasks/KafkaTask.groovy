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
   String servers, serializerKey, serializerValue, acks

   @Input
   @Optional
   String registry

   @TaskAction
   def kafkaTask() {

      def properties = [
              "bootstrap.servers"   : servers,
              // serializers
              "value.serializer"    : serializerValue,
              "key.serializer"      : serializerKey,
              // acknowledgement control
              "acks"                : acks
      ]

      if (registry) {

         properties['schema.registry.url'] = registry
      }

      def producer = new KafkaProducer(properties)

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def topicName = getEntityName(file)

            //todo set to debug
            logger.warn "topic: $topicName"

            def response

            //todo set to debug
            logger.warn "message: ${file.text}"

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

               //todo set to debug
               logger.warn "response: ${response.dump()}"
               logger.warn "result: ${response.result.dump()}"

            }

            return response
         }
      }
   }
}