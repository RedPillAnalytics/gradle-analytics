package com.redpillanalytics.analytics

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class AnalyticsWriter {

   String buildId
   File buildDir
   String dataDirName
   String extension
   Gson gson = new GsonBuilder().serializeNulls().create()


   AnalyticsWriter(String buildId, File buildDir, String dataDirName = 'analytics', String extension = 'json') {

      this.buildId = buildId
      this.buildDir = buildDir
      this.dataDirName = dataDirName
      this.extension = extension

   }

   File getAnalyticsFile(String filename) {

      def analyticsFile = new File(buildDir, "${dataDirName}/${buildId}/${filename}.${extension}")

      return analyticsFile
   }

   def writeData(String filename, def record) {

      def analyticsFile = getAnalyticsFile(filename)

      analyticsFile.parentFile.mkdirs()

      analyticsFile.append(gson.toJson(record) + '\n')
   }
}
