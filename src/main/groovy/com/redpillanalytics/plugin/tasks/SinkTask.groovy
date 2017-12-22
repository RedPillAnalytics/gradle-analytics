package com.redpillanalytics.plugin.tasks

import com.redpillanalytics.common.Utils
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
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
    * Returns the directory where JSON analytics data files are generated.
    * <p>
    * The analytics directory is in the Gradle build directory, and is dependent on the buildId used.
    *
    * @return {@link com.redpillanalytics.plugin.AnalyticsPluginExtension#getAnalyticsDir(java.io.File)}
    */
   @InputDirectory
   File getAnalyticsDir() {

      return project.extensions.analytics.getAnalyticsBaseDir(project.buildDir)
   }
}