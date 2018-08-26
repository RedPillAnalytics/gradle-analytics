package com.redpillanalytics.analytics.sinks

import groovy.util.logging.Slf4j

@Slf4j
class Sink {

   Boolean ignoreErrors

   // constructor
   // default is to ignore errors
   Sink(Boolean ignoreErrors = false) {

      this.ignoreErrors = ignoreErrors

      log.debug "ignoreErrors: ${ignoreErrors.toString()}"
   }
}
