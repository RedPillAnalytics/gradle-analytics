package com.redpillanalytics.common

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException

/**
 * Created by stewartbryson on 11/30/16.
 */
@Slf4j
class GradleUtils {

   static setParameters(Project project, String extension) {

      // Go look for any -P properties that have "extension." in them
      // If so... update the extension value
      project.ext.properties.each { key, value ->

         if (key =~ /$extension\./) {

            def (extensionName, property) = key.toString().split(/\./)

            if (extensionName == extension && project."$extension".hasProperty(property)) {

               log.warn "Setting configuration property for extension: $extension, property: $property, value: $value"

               if (project.extensions.getByName(extension)."$property" instanceof Boolean) {

                  project.extensions.getByName(extension)."$property" = value.toBoolean()
               } else if (project.extensions.getByName(extension)."$property" instanceof Integer) {

                  project.extensions.getByName(extension)."$property" = value.toInteger()
               } else {

                  project.extensions.getByName(extension)."$property" = value
               }
            }
         }
      }
   }

   static getDependency(Project project, String configuration, String regexp) {

      return project.configurations."$configuration".find { File file -> file.absolutePath =~ regexp }
   }

   static isUsableConfiguration(Project project, String configuration, String regexp) {

      try {

         if (getDependency(project, configuration, regexp)) {
            return true
         } else {
            return false
         }
      } catch (UnknownConfigurationException e) {
         return false
      }
   }

   static getParameter(Project project, String name, String extension, String defaultValue = null) {

      // define the value we get along the way
      def value

      def extName = "${extension}.${name}"
      if (project.ext.has(extName)) {

         // give precedence to Gradle properties passed with a dot (.) notation
         // For instance: checkmate.buildId instead of just buildId
         // this will hopefully allow me to gracefully remove the non-dot-notation properties over time

         value = project.ext.get(extName)

      } else if (project.ext.has(name)) {

         // next in order is a non dot-notation parameter
         // I would like to eventually phase this out

         value = project.ext.get(name)

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
