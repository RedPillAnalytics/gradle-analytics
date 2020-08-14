package com.redpillanalytics.analytics.containers

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 12/26/19.
 */
@Slf4j
@InheritConstructors
class ObjectStoreContainer extends SinkContainer {
    /**
     * The Object Store bucket to use. Translates to the particular cloud service provider.
     */
    String bucketName
}
