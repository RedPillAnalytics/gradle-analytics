package com.redpillanalytics.plugin.tasks

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CreateBucketRequest
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

@Slf4j
class S3Task extends ObjectStoreTask {

   @TaskAction
   def s3Task() {

      def client = new AmazonS3Client()

      // first create the bucket

      log.debug "Creating bucket: ${getBucketName()}"
      client.createBucket(new CreateBucketRequest(bucketName))

      def cnt = 0

      getAnalyticsDir().eachFile FileType.DIRECTORIES, { dir ->

         dir.eachFile FileType.FILES, { file ->

            cnt++

            project.logger.debug "bucket: ${prefix}"

            try {

               String object = getFilePath(file, dir)
               def result = client.putObject(getBucketName(), object, file)

               logger.info "Key '$object' uploaded to bucket '${getBucketName()}'."
               logger.debug result.toString()

            } catch (Exception e) {

               if (project.extensions.analytics.ignoreStreamErrors) {

                  logger.info e.toString()

               } else {

                  throw e
               }
            }
         }
      }

      log.warn "$cnt files uploaded to S3."
   }
}