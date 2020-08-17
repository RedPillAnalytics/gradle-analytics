package com.redpillanalytics.analytics.containers

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 12/26/19.
 */
@Slf4j
@InheritConstructors
class FirehoseContainer extends SinkContainer {
    /**
     * The joiner used to construct Firehose stream names.
     *
     * Default: '.'
     */
    String joiner = '.'
}
