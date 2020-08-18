package com.redpillanalytics.analytics.tasks

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import org.gradle.api.tasks.Internal

import static java.nio.charset.StandardCharsets.UTF_8
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

@Slf4j
class GcsTask extends ObjectStoreTask {



   /**
    * Google Cloud storage client used to upload files.
    *
    * @return GCS Storage object.
    */
   @Internal
   def getStorage() {
      return StorageOptions.getDefaultInstance().getService()
   }

   /**
    * Create GCS bucket.
    */
   def createBucket(String name) {
      log.info "Creating bucket: ${name}"
      try {
         storage.create(BucketInfo.of(name))
      }
      catch (StorageException se) {

         if (se.reason == 'conflict') {
            log.info "Bucket ${bucket} already exists."
         } else {
            if (ignoreErrors) {
               log.debug "Exception logged"
               project.logger.info se.toString()
            } else {
               log.debug "Exception thrown"
               throw se
            }
         }
      }
      catch (Exception e) {

         if (ignoreErrors) {
            log.debug "Exception logged"
            project.logger.info e.toString()

         } else {
            log.debug "Exception thrown"
            throw e
         }
      }
   }

   /**
    * Upload file to a GCS bucket.
    */
   def uploadFile(String bucket, File file, String name) {
      try {
         BlobId blobId = BlobId.of(bucket, name)
         BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build()
         storage.create(blobInfo, file.text.getBytes(UTF_8))
      } catch (Exception e) {
         if (ignoreErrors) {
            project.logger.info e.toString()
         } else {
            throw e
         }
      }
   }

   /**
    * The Gradle Custom Task @TaskAction.
    */
   @TaskAction
   def taskAction() {
      createBucket(bucket)

      analyticsFiles.each {file ->
         uploadFile(bucket, file, "${getBucketPath(file)}")
      }
      logSink()
   }
}
