package com.redpillanalytics.plugin.tasks

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

      // first create the bucket
      log.info "Creating bucket: ${getBucketName()}"
      try {

         storage.create(BucketInfo.of(getBucketName()))

      }
      catch (StorageException se) {

         if (se.message == 'You already own this bucket. Please select another name.') {

            log.debug "Bucket ${prefix} already exists."

         } else {

            if (project.analytics.ignoreErrors.toBoolean()) {

               log.warn project.analytics.ignoreErrors.toBoolean().dump()

               log.warn "Exception logged"
               project.logger.info se.toString()

            } else {

               log.warn "Exception thrown"
               throw se
            }
         }
      }
      catch (Exception e) {

         if (project.analytics.ignoreErrors.toBoolean()) {

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

               BlobId blobId = BlobId.of(getBucketName(), "${getFilePath(file, dir)}")
               BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build()
               storage.create(blobInfo, file.text.getBytes(UTF_8))

            } catch (Exception e) {

               if (project.analytics.ignoreErrors.toBoolean()) {

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