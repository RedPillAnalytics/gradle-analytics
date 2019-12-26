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
    * The prefix used to construct sink targets, such as topics, buckets and tables.
    */
   String prefix

   /**
    * The suffix used to construct sink targets, such as topics, buckets and tables.
    */
   String suffix

   /**
    * The joiner used to construct sink targets, such as topics, buckets and tables.
    *
    * Default: '-'
    */
   String joiner

   // capture the debug status
   Boolean isDebugEnabled = log.isDebugEnabled()

   Boolean ignoreErrors, formatSuffix = false

   /**
    * Returns the container name.
    *
    * @return The container name.
    */
   def getContainerType() {
      return ((getClass() =~ /\w+$/)[0] - "Container")
   }

   /**
    * Log a debug message with the name of the container object.
    */
   void logTaskName(String task) {
      log.debug "${getContainerType()} TaskName: $task"
   }

   /**
    * Returns the container name.
    *
    * @return The container name.
    */
   def getTaskName() {
      String taskName = getContainerType().uncapitalize() + name.capitalize() + "Sink"
      logTaskName(taskName)
      return taskName
   }

   def getDescription() {
      return "Process data files using the '${getName()}' delivery sink and '${getPrefix()}' naming prefix."
   }

   def getPrefix() {
      return prefix ?: "gradle"
   }
}
