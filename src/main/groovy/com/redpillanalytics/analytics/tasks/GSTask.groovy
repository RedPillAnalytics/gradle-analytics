package com.redpillanalytics.analytics.tasks

import com.google.cloud.ServiceOptions
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import groovy.io.FileType
import static java.nio.charset.StandardCharsets.UTF_8
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

@Slf4j
@groovy.transform.InheritConstructors
class GSTask extends ObjectStoreTask {

   /**
    * The Gradle Custom Task @TaskAction.
    */
   @TaskAction
   def gsTask() {

      Storage storage = StorageOptions.getDefaultInstance().getService()

      def bucketName = ServiceOptions.getDefaultProjectId() + '-' + getBucketName()

      // first create the bucket
      log.info "Creating bucket: ${bucketName}"
      try {

         storage.create(BucketInfo.of(bucketName))

      }
      catch (StorageException se) {

         if (se.reason == 'conflict') {

            log.info "Bucket ${prefix} already exists."

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

      def cnt = 0

      getAnalyticsDir().eachFile FileType.DIRECTORIES, { dir ->

         dir.eachFile FileType.FILES, { file ->

            cnt++

            // now upload the file

            try {

               BlobId blobId = BlobId.of(bucketName, "${getFilePath(file, dir)}")
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
      }

      log.warn "$cnt files uploaded to GCS."
   }
}