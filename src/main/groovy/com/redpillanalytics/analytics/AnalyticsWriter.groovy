package com.redpillanalytics.analytics

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class AnalyticsWriter {

   String buildId
   File buildDir
   String dataDirName
   Gson gson = new GsonBuilder().serializeNulls().create()


   AnalyticsWriter(String buildId, File buildDir, String dataDirName = 'analytics') {

      this.buildId = buildId
      this.buildDir = buildDir
      this.dataDirName = dataDirName

   }

   File getAnalyticsFile(String filename) {

      def analyticsFile = new File(buildDir, "${dataDirName}/${buildId}/${filename}")

      return analyticsFile
   }

   def writeData(String filename, def record) {

      def analyticsFile = getAnalyticsFile(filename)

      analyticsFile.parentFile.mkdirs()

      analyticsFile.append(gson.toJson(record) + '\n')
   }

   def writeData(String filename, def record, def basicFields) {

      writeData(filename, record << basicFields)
   }
}
