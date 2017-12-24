package com.redpillanalytics.plugin

import com.redpillanalytics.common.CI
import groovy.util.logging.Slf4j

@Slf4j
class AnalyticsPluginExtension {

   /**
    * The organization name for Gradle Analytics.
    * <p>
    * "organization" is added to the JSON data files to enable segmentation of data across different organizations.
    */
   String organization = 'Not Specified'
   /**
    * The name to use for the analytics data directory inside the Gradle build directory.
    */
   String dataDirName = 'analytics'
   /**
    * A unique ID for each Gradle build, used to collect analytics data together for each build.
    * <p>
    * The default value is the current timestamp in the format of 'YYYY-MM-DD-HHMMSSIII'. The buildId can be overridden using CI server Job IDs, for instance.
    */
   String buildId = CI.getTimestamp()
   /**
    * The name to use for the {@code task} JSON data file.
    */
   String tasksFileName = 'task.json'
   /**
    * The name to use for the {@code test} JSON data file.
    */
   String testsFileName = 'test.json'
   /**
    * The name to use for the {@code testoutput} JSON data file.
    */
   String testOutputFileName = 'testoutput.json'
   /**
    * Determines whether errors in producing data to sinks should be ignored.
    */
   Boolean ignoreErrors = true
   /**
    * When true, the {@code produce} task will compress all the JSON data files upon completion.
    */
   Boolean compressFiles = true
   /**
    * When true, the {@code produce} task will delete all the JSON data files upon completion.
    */
   Boolean cleanFiles = true
   /**
    * The hostname of the environment executing Gradle builds. Java is used to pull the current hostname as a default.
    */
   String hostname = java.net.InetAddress.getLocalHost().getHostName()

   /**
    * Returns the base directory where JSON analytic data files are generated.
    * <p>
    * The analytics base directory is in the Gradle build directory.
    *
    * @param buildDir The Gradle build directory
    * @return The directory where JSON data files are generated
    */
   File getAnalyticsBaseDir(File buildDir) {

      return new File(buildDir, "${dataDirName}")
   }

   /**
    * Returns the specific directory where JSON analytic data files are generated.
    * <p>
    * The analytics directory is in the Gradle build directory, and is dependent on the buildId.
    *
    * @param buildDir The Gradle build directory
    * @return The directory where JSON data files are generated
    */
   File getAnalyticsDir(File buildDir) {

      return new File(getAnalyticsBaseDir(buildDir), buildId)
   }

   /**
    * Returns the {@code task} JSON data file.
    * <p>
    * The {@code task} JSON data file contains task-level information about the Gradle build.
    * @param buildDir The Gradle build directory
    * @return The {@code task} JSON data file
    */
   File getTasksFile(File buildDir) {

      return new File(getAnalyticsDir(buildDir), tasksFileName)
   }

   /**
    * Returns the {@code test} JSON data file.
    * <p>
    * The {@code test} JSON data file contains information about each unit test.
    * @param buildDir The Gradle build directory
    * @return The {@code test} JSON data file
    */
   File getTestsFile(File buildDir) {

      return new File(getAnalyticsDir(buildDir), testsFileName)
   }

   /**
    * Returns the {@code testOutput} JSON data file.
    * <p>
    * The {@code testOutput} JSON data file contains the standard output generated by each unit test.
    * @param buildDir The Gradle build directory
    * @return The {@code testOutput} JSON data file
    */
   File getTestOutputFile(File buildDir) {

      return new File(getAnalyticsDir(buildDir), testOutputFileName)
   }

}
