package com.redpillanalytics.analytics.records

class BuildRecord extends HeaderRecord {

   String   hostname
   String   commithash
   String   scmbranch
   String   repositoryurl
   String   commitemail
   String   rootprojectname
   String   rootprojectdir
   String   rootbuilddir
   String   version
   String   builddate
   Integer  duration
   String   status
   String   stacktrace
}
