package com.redpillanalytics.analytics.containers

import com.redpillanalytics.analytics.containers.AnalyticsContainer
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 11/19/16.
 */
@Slf4j
class SinkContainer extends AnalyticsContainer {

   // naming
   String prefix, sink

   // JDBC connection information
   String username, password, driverUrl, driverClass

   // Kafka properties
   String name, servers, serializerKey, serializerValue, acks

   Boolean ignoreErrors

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
