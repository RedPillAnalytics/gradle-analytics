package common

import groovy.util.logging.Slf4j
import org.gradle.api.Project

/**
 * Created by stewartbryson on 11/30/16.
 */
@Slf4j
class GradleUtils {

   static public getParameter(Project project, String name, String extension, String defaultValue = null) {

      // define the value we get along the way
      def value

      def extName = "${extension}.${name}"
      if (project.ext.has(extName)) {

         value = project.ext.get(extName)

      } else if (CI.getBuildParameter(name)) {

         // next are non dot notation environment variables
         // note: we support the Bamboo weird way of doing variables
         value = CI.getBuildParameter(name)
      } else {

         // next we return values from the custom extension
         value = project.extensions.getByName(extension).properties.get(name, defaultValue)

      }

      // we want to update the value in the extension in these cases
      // this way, listeners, other plugins, etc. can all use them
      if (project.extensions."$extension".hasProperty(name)) {

         project.extensions.getByName(extension)."$name" = value
      }

      // finally return it
      return value
   }

}
