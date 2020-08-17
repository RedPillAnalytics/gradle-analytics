package com.redpillanalytics.analytics.containers

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

/**
 * Created by stewartbryson on 08/14/20.
 */
@Slf4j
@InheritConstructors
class BigQueryContainer extends ObjectStoreContainer {
    /**
     * Returns the task name.
     *
     * @return The task name.
     */
    def getTaskName() {
        String taskName = 'bq' + name.capitalize() + "Sink"
        logTaskName(taskName)
        return taskName
    }

    /**
     * The BigQuery dataset to use.
     */
    String dataset = 'gradle_analytics'

    /**
     * The joiner used to construct sink targets, such as topics, buckets and tables.
     *
     * Default: '_'
     */
    String joiner = '_'
}
