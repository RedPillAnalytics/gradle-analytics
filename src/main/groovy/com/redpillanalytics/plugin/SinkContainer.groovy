package com.redpillanalytics.plugin

import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 11/19/16.
 */
@Slf4j
class SinkContainer extends DomainContainer {

    // naming
    String prefix, mechanism

    // JDBC connection information
    String username, password, driverUrl, driverClass

    SinkContainer(String name) {

        this.name = name
    }

    def getDescription() {

        return "Process data files using the '${getMechanism()}' delivery mechanism and '${getPrefix()}' naming prefix."
    }

    def getMechanism() {

        def mechanism = this.mechanism ?: name
        log.debug "mechanism: ${mechanism}"
        assert ['s3', 'firehose', 'pubsub', 'jdbc', 'gs'].contains(mechanism)
        return mechanism
    }

    def getPrefix() {

        return prefix ?: "gradle.${name}"
    }

}
