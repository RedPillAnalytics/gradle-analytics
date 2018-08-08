package com.redpillanalytics.analytics.containers

import groovy.util.logging.Slf4j

@Slf4j
class AnalyticsContainer {

   /**
    * The name of the container entity.
    */
   String name

   String servers

   String serializerKey

   String serializerValue

   String acks

   // capture the debug status
   Boolean isDebugEnabled = log.isDebugEnabled()

   def getDomainName() {

      return ((getClass() =~ /\w+$/)[0] - "Container")
   }

   def logTaskName(String task) {

      log.debug "${getDomainName()}: $name, TaskName: $task"

   }

   def getTaskName(String baseTaskName) {

      String taskName = name + baseTaskName.capitalize()

      logTaskName(taskName)

      return taskName

   }

   def getTaskGroup(String groupName) {

      return groupName
   }

}
