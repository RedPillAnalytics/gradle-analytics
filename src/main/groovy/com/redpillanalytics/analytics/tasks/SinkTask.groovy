package com.redpillanalytics.analytics.tasks

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory

@Slf4j
class SinkTask extends DefaultTask {

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.SinkContainer#prefix}
    */
   @Input
   @Optional
   @Option(option = "prefix",
           description = "A prefix to use when creating or producing to Sink entities.")
   String prefix

   /**
    * Configured using {@link com.redpillanalytics.analytics.containers.SinkContainer#joiner}
    */
   @Input
   @Optional
   @Option(option = "joiner",
           description = "The joiner string used to construct sink entity names.")
   String joiner

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
    * Returns the entity name (i.e. topic name, stream name, etc.) to use with the particular Sink technology.
    *
    * @param file The particular File object to send to the Sink
    * @param joiner The specific character to use in the join operation when constructing the entity name
    * @return The entity name (i.e. topic name, stream name, etc.) to use with the particular Sink technology
    */
   String getEntityName(File file, String joiner='-') {
      def entityName = FilenameUtils.getBaseName(file.name)
      log.debug "Filebase: $entityName, prefix: $prefix, suffix: $suffix"
      if (prefix) {entityName = [prefix, entityName].join(joiner)}
      if (suffix) {entityName = [entityName, suffix].join(joiner)}
      log.debug "Sink entity: $entityName"
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