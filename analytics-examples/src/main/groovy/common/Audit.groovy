package com.redpillanalytics.common

import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 8/28/16.
 */
@Slf4j
class Audit {

    def output = new ByteArrayOutputStream()
    def error = new ByteArrayOutputStream()


    def logInfo(Boolean keep = false) {

        log.info(output.toString())

        if (!keep) {

            output.reset()
        }
    }


    def logError(Exception e, Boolean keep = false) {

        log.info(error.toString())

        if (!keep) {

            error.reset()
        }

        throw e
    }

}
