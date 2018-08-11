package com.redpillanalytics.analytics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.redpillanalytics.common.CI
import org.apache.avro.Schema
import org.apache.avro.reflect.ReflectData
import tech.allegro.schema.json2avro.converter.JsonAvroConverter

import static java.util.UUID.randomUUID
import groovy.util.logging.Slf4j

@Slf4j
class AnalyticsPluginExtension {

   String schemaHeader = '''
         { "name": "buildid", "type": "string" },
         { "name": "buildtag", "type": "string" },
         { "name": "organization", "type": "string" }
         '''.stripIndent()

   String testSchema = """
         {
           "type" : "record",
           "name" : "Test",
           "fields" : [
                        ${schemaHeader}
                        { "name": "projectname", "type": "string" },
                        { "name": "projectdir", "type": "string" },
                        { "name": "builddir", "type": "string" },
                        { "name": "buildfile", "type": "string" },
                        { "name": "testname", "type": "string" },
                        { "name": "classname", "type": "string" },
                        { "name": "starttimee", "type": "string" },
                        { "name": "endtime", "type": "string" },
                        { "name": "executioncount", "type": "int" },
                        { "name": "successcount", "type": "int" },
                        { "name": "failcount", "type": "int" },
                        { "name": "skipcount", "type": "int" },
                      ]
          }
          """.stripIndent()

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
   String buildId = randomUUID().toString()
   /**
    * A unique ID for each CI Server build, which might encompass multiple Gradle executions.
    * <p>
    * The default value is the build tag from known CI servers, and if none are detected, then it uses {@link #buildId}.
    */
   String buildTag = CI.getBuildTag()
   /**
    * The format to use when writing the output data.
    * <p>
    * The default value is 'JSON', but it also supports 'Avro'.
    */
   String format = 'JSON'
   /**
    * The name to use for the {@code build} JSON data file.
    */
   String buildsFileName = 'build.json'
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
   Boolean ignoreErrors = false
   /**
    * When true, the {@code producer} task will compress all the JSON data files upon completion.
    */
   Boolean compressFiles = true
   /**
    * When true, the {@code producer} task will delete all the JSON data files upon completion.
    */
   Boolean cleanFiles = true
   /**
    * The hostname of the environment executing Gradle builds. Java is used to pull the current hostname as a default.
    */
   String hostname = java.net.InetAddress.getLocalHost().getHostName()
   /**
    * The commit hash of the Git repository for the Gradle build.
    */
   String gitCommitHash = CI.commitHash
   /**
    * The branch of the Git repository for the Gradle build.
    */
   String gitBranch = CI.getBranch()
   /**
    * The URL of the remote Git repository, as we can best determine it.
    */
   String gitRepositoryUrl = CI.getRepositoryUrl()
   /**
    * The email address associated with the commit for the Git repository.
    */
   String gitCommitEmail = CI.getCommitEmail()
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

   File getAnalyticsFile(String filename, File buildDir) {

      return new File(filename, getAnalyticsDir(buildDir))
   }

   def getHeaderJson() {

      return [buildid     : buildId,
              buildTag    : buildTag,
              organization: organization]
   }

   def writeAnalytics(String filename, File buildDir, def record, Boolean useHeaders = false) {

      def message

      def analyticsFile = getAnalyticsFile(filename, buildDir)

      analyticsFile.parentFile.mkdirs()

      Gson gson = new GsonBuilder().serializeNulls().create()

      if (useHeaders) {

         message = getHeaderJson() + record
      }

      message = message + '\n'

      if (getFormat().toLowerCase() == 'avro') {

         analyticsFile.append(new JsonAvroConverter().convertToAvro((gson.toJson(record)).getBytes(), ReflectData.get().getSchema(getTestSchema())))
      }
      else {

         analyticsFile.append(gson.toJson(record))
      }
   }

}
