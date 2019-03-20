/**
 * Created by stewart on 4/30/15.
 */

package com.redpillanalytics.analytics.tasks

import com.redpillanalytics.common.Utils
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class JdbcTask extends SinkTask {

   @Input
   @Option(option = "username",
           description = "Username for making the JDBC connection.")
   String username

   @Input
   @Option(option = "password",
           description = "Password for making the JDBC connection.")
   String password

   @Input
   String driverUrl, driverClass

   def getStreamName(File file) {
      return [prefix, Utils.getFileBase(file)].join('.').replace('-', '_') + '_stream'
   }

   @TaskAction
   def jdbcTask() {

      project.configurations.analytics.files { it.name =~ '.*jdbc.*' }.each {
         Sql.classLoader.addURL(it.toURI().toURL())
      }

      def db = Sql.newInstance(
              driverUrl,
              username,
              password,
              driverClass
      )

      analyticsFiles.each { file ->
         def table = getStreamName(file)
         file.each { record ->
            def statement = "insert into " + table + "(data) values('${record}')"
            log.debug("Statement: $statement")
            try {
               db.execute(statement)
            }
            catch (Exception e) {
               if (ignoreErrors) {
                  log.debug "Exception logged"
                  project.logger.info e.toString()
               } else {
                  log.debug "Exception thrown"
                  throw e
               }
            }
         }
         logSink()
      }
      db.close()
   }
}
