package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.common.Utils
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@Slf4j
@groovy.transform.InheritConstructors
class ObjectStoreTask extends SinkTask {

   /**
    * Returns the name of the bucket to use in the Object Store.
    * <p>
    * @return The name of the bucket in the Object Store.
    */
   @Input
   @Optional
   String getBucketName() {
      log.debug "Name of bucket: $prefix"
      return prefix
   }

   /**
    * Returns the path of the file inside the bucket to use in the Object Store.
    * <p>
    * @return The path of the file inside the Object Store.
    */
   String getFilePath(File file, File dir) {
      log.debug "Original file: ${file.absolutePath}"
      def fileName = "${Utils.getFileBase(file)}/$dir.name.${Utils.getFileExt(file)}"
      log.debug "Name of file: $fileName"
      return fileName
   }
}