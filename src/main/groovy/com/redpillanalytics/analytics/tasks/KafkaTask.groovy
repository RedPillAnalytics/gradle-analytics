package com.redpillanalytics.analytics.tasks

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

// Annotation for a logger
@Slf4j
class KafkaTask extends SinkTask {

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.KafkaContainer#bootstrapServers}
    */
   @Input
   @Option(option = "bootstrap-servers",
           description = "The value for 'bootstrap.servers' in the KafkaProducer API.")
   String bootstrapServers

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.KafkaContainer#serializerKey}
    */
   @Input
   @Option(option = "serializer-key",
           description = "The value for 'serializer.key' in the KafkaProducer API.")
   String serializerKey

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.KafkaContainer#serializerValue}
    */
   @Input
   @Option(option = "serializer-value",
           description = "The value for 'serializer.value' in the KafkaProducer API.")
   String serializerValue

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.KafkaContainer#acks}
    */
   @Input
   @Option(option = "acks",
           description = "The value for 'acks' in the KafkaProducer API.")
   String acks

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.KafkaContainer#schemaRegistry}
    */
   @Input
   @Optional
   @Option(option = "schema-registry",
           description = "The value for 'schema.registry.url' in the KafkaProducer API.")
   String schemaRegistry

   @Input
   @Optional
   String registry

   @TaskAction
   def kafkaTask() {

      def properties = [
              "bootstrap.servers": bootstrapServers,
              // serializers
              "value.serializer" : serializerValue,
              "key.serializer"   : serializerKey,
              // acknowledgement control
              "acks"             : acks
      ]

      if (registry) {
         properties['schema.registry.url'] = schemaRegistry
      }
      def producer = new KafkaProducer(properties)

      analyticsFiles.each { file ->
         log.debug "file: $file"

         def topicName = getEntityName(file, joiner ?: '_')
         log.debug "topic: $topicName"

         def response
         log.debug "message: ${file.text}"

         try {
            response = producer.send(new ProducerRecord(topicName, file.text))
         }
         catch (Exception e) {
            if (ignoreErrors) {
               log.info e.toString()
            } else {
               throw e
            }
         }
         finally {
            log.debug "response: ${response.dump()}"
            log.debug "result: ${response.result.dump()}"
         }
         return response
      }

      logSink()
   }
}