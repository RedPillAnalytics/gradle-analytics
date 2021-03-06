package com.redpillanalytics.analytics.tasks

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CreateBucketRequest
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

@Slf4j
class S3Task extends ObjectStoreTask {

   @TaskAction
   def s3Task() {
      def client = new AmazonS3Client()

      // first create the bucket
      log.info "Creating bucket: ${bucket}"
      client.createBucket(new CreateBucketRequest(bucket))

      analyticsFiles.each { file ->
         project.logger.debug "bucket: ${bucket}"

         try {
            String object = getBucketPath(file)
            def result = client.putObject(getBucket(), object, file)
            log.info "Key '$object' uploaded to bucket '${bucket}'."
            log.debug result.toString()
         } catch (Exception e) {
            if (project.analytics.ignoreErrors.toBoolean()) {
               log.info e.toString()
            } else {
               throw e
            }
         }
      }
      logSink()
   }
}