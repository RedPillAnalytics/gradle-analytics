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
class ConfluentTask extends SinkTask {

   @Input
   String restUrl

   @TaskAction
   def confluentTask() {

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def topicName = getEntityName(file)

            //todo set to debug
            logger.warn "topic: $topicName"

            def response

            //todo set to debug
            logger.warn "message: ${file.text}"

            try {

               // need REST logic for passing file.text to topicName
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
               //logger.warn "response: ${response.dump()}"
               //logger.warn "result: ${response.result.dump()}"

            }

            //return response
         }
      }
   }
}