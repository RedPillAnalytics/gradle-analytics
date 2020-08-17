package com.redpillanalytics.analytics.tasks

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

@Slf4j
class ObjectStoreTask extends SinkTask {

   /**
    * The Object Store bucket to use. Translates to the particular cloud service provider.
    */
   @Input
   @Option(option = "bucket",
           description = "The Object Store bucket to use. Translates to the particular cloud service provider."
   )
   String bucket

   /**
    * Returns the path of the file inside the bucket to use in the Object Store.
    * <p>
    * @return The path of the file inside the Object Store.
    */
   String getBucketPath(File file) {
      log.debug "File: ${file.absolutePath}"
      def fileName = "${FilenameUtils.getBaseName(file.name)}/${file.parentFile.name}.${FilenameUtils.getExtension(file.name)}"
      log.debug "Bucket path: $fileName"
      return fileName
   }
}
