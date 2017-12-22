package com.redpillanalytics.sinks

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 11/20/16.
 */
@Slf4j
class Sink {

    Boolean ignoreErrors
    Gson gson

    // constructor
    // default is to ignore errors
    Sink(Boolean ignoreErrors=true) {

        this.ignoreErrors = ignoreErrors
        this.gson = new GsonBuilder().serializeNulls().create()
    }

    def jsonToDelimited(String data, String delimiter='|') {

        def pipeData = gson.fromJson(data, Map).values().join(delimiter)

        return pipeData

    }

    def objectToJson(Object object) {

        def json = gson.toJson(object)

        log.debug "json document: ${JsonOutput.prettyPrint(json)}"

        return json
    }

    def getTimestamp() {

        return new Date().format('yyyy-MM-dd-HHmmssSS')
    }

}
