package com.redpillanalytics.plugin.tasks

import com.redpillanalytics.common.Utils
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

@Slf4j
@groovy.transform.InheritConstructors
class SinkTask extends DefaultTask {

   /**
    * Configured using {@link com.redpillanalytics.plugin.SinkContainer#getPrefix()}
    */
   @Input
   String prefix

   /**
    * Configured using {@link com.redpillanalytics.plugin.SinkContainer#getIgnoreErrors()}
    */
   @Input
   @Option(option = "ignoreErrors",
           description = "determines whether errors returned from the Sink are ignored or not.",
           order = 1)
   Boolean ignoreErrors = false

   /**
    * Returns the directory where JSON analytics data files are generated.
    * <p>
    * The analytics directory is in the Gradle build directory, and is dependent on the buildId used.
    *
    * @return {@link com.redpillanalytics.plugin.AnalyticsPluginExtension#getAnalyticsDir(java.io.File)}
    */
   @InputDirectory
   File getAnalyticsDir() {

      return project.analytics.getAnalyticsBaseDir(project.buildDir)
   }

   /**
    * Returns the entity name (i.e. topic name, stream name, etc.) to use with the particular Sink technology..
    * <p>
    *
    * @return String
    */

   /**
    * Returns the entity name (i.e. topic name, stream name, etc.) to use with the particular Sink technology.
    *
    * @param file The particular File object to send to the Sink
    * @param joiner The specific character to use in the join operation when constructing the entity name
    * @return The entity name (i.e. topic name, stream name, etc.) to use with the particular Sink technology
    */
   @Input
   String getEntityName(File file, String joiner='.') {

      def entityName = [prefix, Utils.getFileBase(file)].join(joiner)
      log.debug "Name of the topic: $entityName"
      return entityName

   }
}