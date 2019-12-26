package com.redpillanalytics.analytics.containers

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 12/25/19.
 */
@Slf4j
@InheritConstructors
class KafkaContainer extends SinkContainer {

   /**
    * The value for 'bootstrap.servers' in the KafkaProducer API.
    */
   String bootstrapServers

   /**
    * The value for 'key.serializer' in the KafkaProducer API.
    */
   String serializerKey

   /**
    * The value for 'value.serializer' in the KafkaProducer API.
    */
   String serializerValue

   /**
    * The value for 'acks' in the KafkaProducer API.
    */
   String acks

   /**
    * The value for 'schema.registry.url' in the KafkaProducer API.
    */
   String schemaRegistry
}
