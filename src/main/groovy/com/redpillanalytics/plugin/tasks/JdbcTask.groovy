/**
 * Created by stewart on 4/30/15.
 */

package com.redpillanalytics.plugin.tasks

import com.redpillanalytics.sinks.Stream
import com.redpillanalytics.common.Utils
import groovy.sql.Sql
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

// Annotation for a logger
@Slf4j
@groovy.transform.InheritConstructors
class JdbcTask extends SinkTask {

   @Input
   @Option(option = "username",
           description = "username for making the JDBC connection.",
           order = 1)
   String username

   @Input
   @Option(option = "password",
           description = "password for making the JDBC connection.",
           order = 2)
   String password

   @Input
   String driverUrl, driverClass

   def getStreamName(File file) {

      return [prefix, Utils.getFileBase(file)].join('.').replace('-', '_') + '_stream'

   }

   @TaskAction
   def jdbcTask() {

      def stream = new Stream(project.extensions.checkmate.ignoreStreamErrors)

      project.configurations.analytics.files { it.name =~ '.*jdbc.*' }.each {
         Sql.classLoader.addURL(it.toURI().toURL())
      }

      def db = Sql.newInstance(
              driverUrl,
              username,
              password,
              driverClass
      )

      getAnalyticsDir().eachFile(FileType.DIRECTORIES) { dir ->

         dir.eachFile(FileType.FILES) { file ->

            def table = getStreamName(file)

            file.each { record ->

               def statement = "insert into " + table + "(data) values('${record}')"

               logger.debug("Statement: $statement")

               db.execute(statement)
            }

            logger.info "JDBC record(s) sent to '${table}'"
         }
      }

      db.close()
   }
}
