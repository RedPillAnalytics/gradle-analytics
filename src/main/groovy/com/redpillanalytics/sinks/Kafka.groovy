package com.redpillanalytics.sinks

import groovy.util.logging.Slf4j
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
@groovy.transform.InheritConstructors
class Kafka extends Sink {

   def servers = 'localhost:9092'
   def serializerKey = "org.apache.kafka.common.serialization.StringSerializer"
   def serializerValue = "org.apache.kafka.common.serialization.StringSerializer"
   def acks = 'all'

   /**
    * Publish a message to a Kafka topic.
    *
    * @param topicId The topic name to create
    * @param message The message to send to the topic
    * @return The message future object
    */
   def publishTopic(String topicId, String message) {

      def producer = new KafkaProducer([
              "bootstrap.servers": servers,
              // serializers
              "value.serializer" : serializerValue,
              "key.serializer"   : serializerKey,
              // acknowledgement control
              "acks"             : acks
      ])

      def response = producer.send(new ProducerRecord(topicId, message))

      log.warn "response: $response"

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
