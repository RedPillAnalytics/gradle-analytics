package com.redpillanalytics.common

import groovy.util.logging.Slf4j

@Slf4j

class Utils {

   static public getMatchingFiles(File sourceDir, String pattern) {

      // return a list of File objects that match a particular extension
      return sourceDir.listFiles([accept: { file -> file ==~ /$pattern/ }] as FileFilter).toList()

   }

   static public getFileExt(String file) {

      // return a list of File objects that match a particular extension
      return file.tokenize('.').last()

   }

   static public getFileExt(File file) {

      // return a list of File objects that match a particular extension
      return getFileExt(file.name)

   }

   static public getFileBase(File file) {

      return file.name.tokenize('.').first()
   }

   static public getFilesByExt(File fileDir, String fileExt) {

      log.debug("fileDir: $fileDir")
      log.debug("fileExt: $fileExt")

      return fileDir.listFiles([accept: { file -> file ==~ /.*?\.$fileExt/ }] as FileFilter).toList().sort()

   }

   static public getFilesByBasename(File fileDir, String basename) {

      log.debug("fileDir: $fileDir")
      log.debug("basename: $basename")

      return fileDir.listFiles([accept: { file -> file ==~ /\.$basename\..*?/ }] as FileFilter).toList().sort()

   }

   static public getModifiedFileName(File file, String fileExt) {

      log.debug("original file: $file")
      log.debug("fileExt: $fileExt")

      // returns file with a different extension
      def returnFile = file.name.replaceFirst(~/\.[^\.]+$/, ".$fileExt")

      log.debug("file: $returnFile")

      return returnFile

   }

   static public getRenamedFileName(File file, String source, String target) {

      log.debug("original file: $file")

      // returns file with a different basename
      def fileName = file.name.replace(source, target)

      log.debug("file: $fileName")

      return fileName

   }

   static public getRenamedFile(File file, String source, String target) {

      return new File(file.parentFile, getRenamedFileName(file, source, target))

   }


   static public getModifiedFile(File file, File fileDir, String fileExt) {

      log.debug("fileDir: $fileDir")

      // returns a file with the same basename, but with a different path location, and different extension
      return new File(fileDir, getModifiedFileName(file, fileExt))

   }

   static public getModifiedFile(File file, File fileDir) {

      log.debug("file: $file")
      log.debug("fileDir: $fileDir")

      // returns a file with the same basename, but with a different path location
      return new File(fileDir, file.name)

   }

   static public getModifiedFile(File file, String fileExt) {

      // returns a file with the same basename, same path, but different extension
      return new File(file.parent, getModifiedFileName(file, fileExt))

   }

   static public getModifiedFiles(File fileDir, String currentExtension, String newExtension) {

      return fileDir.listFiles([accept: { file -> file ==~ /.*?\.$currentExtension/ }] as FileFilter).toList().collect {

         file -> getModifiedFile(file, newExtension)

      }

   }

   static public getModifiedFiles(File currentDir, String currentExtension, File newDir, String newExtension) {

      def newFiles = currentDir.listFiles([accept: { file -> file ==~ /.*?\.$currentExtension/ }] as FileFilter).toList().collect {

         file -> getModifiedFile(file, newDir, newExtension)

      }

      return newFiles

   }

   static
   public getModifiedMatchingFiles(File currentDir, String currentExtension, File newDir, String newExtension, File matchDir, String matchExtension) {

      // On the left side of the intersect, I'm finding all files matching $currentExtension
      // I'm using the collect with getModifiedFile to get a file object with a new directory and new extension for each file
      // On the right side of the intersect, I'm testing to make sure these are actual files that exist.

      def newFiles = currentDir.listFiles([accept: { file -> file ==~ /.*?\.$currentExtension/ }] as FileFilter).toList().collect {

         file -> getModifiedFile(file, newDir, newExtension)

      }

      //intersect(matchDir.listFiles([accept: { file -> file ==~ /.*?\.$matchExtension/ }] as FileFilter).toList())
      return newFiles

   }

   static public exec(List command, File workingDir = null) {

      log.info("Utils Command: " + command.join(' '))
      log.info("Working Directory: $workingDir")

      def proc = command.execute(null, workingDir)

      proc.waitFor()

      log.info proc.in.text
      log.debug proc.err.text

   }

   static public copy(File source, File target) {

      // copy the files
      target.bytes = source.bytes
      log.info "$source.canonicalPath file copied to $target.canonicalPath"
   }

   static public getMatchingFilesExt(File sourceDir, String sourceExt) {

      // return a list of File objects that match a particular extension
      return sourceDir.listFiles([accept: { file -> file ==~ /.*?\.$sourceExt/ }] as FileFilter).toList()

   }

   static public compareFiles(File baseFile, File compareFile) {

      // need to put in some logic to make sure the basename of the files are the same
      // raise an exception if they are not
      // the current logic here hasn't been tested
      assert baseFile.getName().replaceFirst(~/\.[^\.]+$/, null) == compareFile.getName().replaceFirst(~/\.[^\.]+$/, null)

      // compare "trimmed" versions of the files
      assert baseFile.text.trim() == compareFile.text.trim()

   }

   static public getToolExt() {

      return '.' + (System.getProperty("os.name").contains('Windows') ? 'cmd' : 'sh')

   }


   static public getModifiedBranch(String branchName) {

      if (!branchName) {

         return null

      } else if (CI.isJenkinsCI() && !(branchName =~ /(.+)(\/)/)) {

         return getJenkinsRemote() + '/' + branchName

      } else {

         return branchName
      }
   }

   static public getJenkinsRemote() {

      if (!CI.isJenkinsCI())

         return null

      else

         return System.getenv('GIT_BRANCH') - ~/\/.+/
   }


   static public getRelativePath(File root, File full) {

      return root.toURI().relativize(full.toURI()).toString()

   }

   static public getHostname() {

      return (new InetAddress().getLocalHost().getCanonicalHostName()) ?: 'localhost'


   }

}

