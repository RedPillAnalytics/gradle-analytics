package com.redpillanalytics.plugin.tasks

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import groovy.io.FileType
import org.gradle.api.tasks.Optional
import static java.nio.charset.StandardCharsets.UTF_8
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@Slf4j
@groovy.transform.InheritConstructors
class GSTask extends ObjectStoreTask {

   /**
    * Returns the name of the bucket to use in Google Cloud Storage.
    * <p>
    * Normalizes the name to ensure all characters are supported by Google Cloud Storage.
    *
    * @return The name of the bucket in Google Cloud Storage.
    */
   @Input
   @Optional
   @Override
   String getBucketName() {

      String bucketName = prefix.replace('.', '-')

      log.debug "Name of bucket: $bucketName"

      return bucketName

   }

   /**
    * The Gradle Custom Task @TaskAction.
    */
   @TaskAction
   def gsTask() {

      Storage storage = StorageOptions.getDefaultInstance().getService()

      // first create the bucket
      log.debug "Creating bucket: ${getBucketName()}"
      try {

         storage.create(BucketInfo.of(getBucketName()))

      }
      catch (StorageException se) {

         if (se.message == 'You already own this bucket. Please select another name.') {

            log.info "Bucket ${prefix} already exists."

         } else {

            throw se
         }
      }
      catch (Exception e) {

         if (project.extensions.analytics.ignoreStreamErrors) {

            project.logger.info e.toString()

         } else {

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
               Blob blob = storage.create(blobInfo, file.text.getBytes(UTF_8))

            } catch (Exception e) {

               if (project.extensions.analytics.ignoreStreamErrors) {

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