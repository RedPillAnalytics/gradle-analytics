package com.redpillanalytics.analytics.containers

import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 11/19/16.
 */
@Slf4j
class SinkContainer extends AnalyticsContainer {

   // naming
   String prefix, sink, suffix

   // RESTful URL for any Sink that has one
   String restUrl

   // JDBC connection information
   String username, password, driverUrl, driverClass

   // Kafka properties
   String name, servers, serializerKey, serializerValue, acks, registry

   Boolean ignoreErrors, formatSuffix=false

   SinkContainer(String name) {

      this.name = name
   }

   def getDescription() {

      return "Process data files using the '${getSink()}' delivery sink and '${getPrefix()}' naming prefix."
   }

   def getSink() {

      def sink = this.sink ?: name
      log.debug "sink: ${sink}"
      assert ['s3', 'firehose', 'pubsub', 'jdbc', 'gs', 'kafka'].contains(sink)
      return sink
   }

   def getPrefix() {

      return prefix ?: "gradle"
   }
}