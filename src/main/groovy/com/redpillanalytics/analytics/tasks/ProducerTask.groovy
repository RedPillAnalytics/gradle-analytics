package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.common.Utils
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

@Slf4j
@groovy.transform.InheritConstructors
class ProducerTask extends DefaultTask {

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.SinkContainer#prefix}
    */
   @Input
   @Option(option = "prefix",
           description = "A prefix to use when creating or producing to Sink entities.")
   String prefix

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.SinkContainer#suffix}
    */
   @Input
   @Optional
   @Option(option = "suffix",
           description = "A suffix to use when creating or producing to Sink entities.")
   String suffix

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.SinkContainer#ignoreErrors}
    */
   @Input
   @Option(option = "ignore-errors",
           description = "Determines whether errors returned from the Sink are ignored.")

   Boolean ignoreErrors = project.analytics.ignoreErrors

   /**
    * Returns the directory where JSON analytics data files are generated.
    * <p>
    * The analytics directory is in the Gradle build directory, and is dependent on the buildId used.
    *
    * @return {@link com.redpillanalytics.analytics.AnalyticsPluginExtension#getAnalyticsDir(java.io.File)}
    */
   @InputDirectory
   File getAnalyticsDir() {
      def dir = project.analytics.getAnalyticsBaseDir(project.buildDir)
      log.debug "analytics directory: $dir"
      return dir
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
   String getEntityName(File file, String joiner='.') {

      def entityName = [prefix, Utils.getFileBase(file)].join(joiner)
      entityName = suffix ? [entityName, suffix].join(joiner) : entityName
      log.debug "Name of the sink entity: $entityName"
      return entityName

   }

   /**
    * Gets the hierarchical collection of analytics files, sorted using folder structure and alphanumeric logic.
    *
    * @return The List of analytics files.
    */
   @InputFiles
   List getAnalyticsFiles() {

      def tree = project.fileTree(dir: analyticsDir, includes: ['**/*.json'])
      return tree.sort()
   }

   /**
    * Provide generic logging upon the completion of a sink task.
    */
   def logSink() {
      analyticsFiles.each{ file ->
         log.info "Analytics file $file processed."
      }
      log.warn "${analyticsFiles.size()} analytics files processed."
   }
}