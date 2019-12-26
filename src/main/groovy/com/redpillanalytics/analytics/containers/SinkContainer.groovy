package com.redpillanalytics.analytics.containers

import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 11/19/16.
 */
@Slf4j
class SinkContainer {

   /**
    * The name of the container entity.
    */
   SinkContainer(String name) {
      this.name = name
   }

   /**
    * The name of the container entity.
    */
   String name

   /**
    * The joiner used to construct sink locations, such as topics, buckets and tables.
    *
    * Default: '-'
    */
   String joiner

   // capture the debug status
   Boolean isDebugEnabled = log.isDebugEnabled()

   /**
    * Returns the container name.
    *
    * @return The container name.
    */
   def getDomainName() {
      return ((getClass() =~ /\w+$/)[0] - "Container")
   }

   /**
    * Log a debug message with the name of the container object.
    */
   void logTaskName(String task) {
      log.debug "${getDomainName()} TaskName: $task"
   }

   /**
    * Returns the container name.
    *
    * @return The container name.
    */
   def getTaskName() {
      String taskName = getDomainName().uncapitalize() + name.capitalize()
      logTaskName(taskName)
      return taskName
   }

   String prefix, sink, suffix

   // RESTful URL for any Sink that has one
   String restUrl

   // JDBC connection information
   String username, password, driverUrl, driverClass

   Boolean ignoreErrors, formatSuffix = false

   def getDescription() {
      return "Process data files using the '${getSink()}' delivery sink and '${getPrefix()}' naming prefix."
   }

   def getSink() {
      def sink = this.sink ?: name
      log.debug "sink: ${sink}"
      return sink
   }

   def getPrefix() {
      return prefix ?: "gradle"
   }
}
