package com.redpillanalytics.analytics.tasks

import com.google.cloud.bigquery.BigQueryException
import com.google.cloud.bigquery.Dataset
import com.google.cloud.bigquery.DatasetInfo
import com.google.cloud.bigquery.FormatOptions
import com.google.cloud.bigquery.Job
import com.google.cloud.bigquery.JobInfo
import com.google.cloud.bigquery.JobStatistics
import com.google.cloud.bigquery.LoadJobConfiguration
import com.google.cloud.bigquery.TableId
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import com.google.cloud.bigquery.BigQueryOptions
import org.gradle.api.tasks.options.Option

@Slf4j
class BigQueryTask extends GcsTask {

    /**
     * The BigQuery dataset name to use.
     */
    @Input
    @Option(option = "dataset",
            description = "The BigQuery dataset name to use."
    )
    String dataset


    /**
     * BigQuery client used to send requests.
     */
    @Internal
    def getBigquery() {
        return BigQueryOptions
                .getDefaultInstance()
                .getService()
    }

    /**
     * Create a BigQuery dataset.
     */
    def createDataset(String name) {
        try {
            DatasetInfo datasetInfo = DatasetInfo.newBuilder(name).build()
            Dataset dataset = bigquery.create(datasetInfo)
            String newDatasetName = dataset.getDatasetId().getDataset()
            log.info "Dataset '$newDatasetName' created successfully"
        } catch (BigQueryException e) {
            if (e.message.contains('Already Exists: Dataset')) {
                log.warn "Datset '${name}' already exists."
            } else {
                throw e
            }
        }
    }

    /**
     * Load a BigQuery table.
     */
    def loadTable(String table, String uri) {

        TableId tableId = TableId.of(dataset, table)
        log.debug "TableId: ${tableId}"

        LoadJobConfiguration configuration =
                LoadJobConfiguration.newBuilder(tableId, uri)
                        .setAutodetect(true)
                        .setFormatOptions(FormatOptions.json())
                        .setCreateDisposition(JobInfo.CreateDisposition.CREATE_IF_NEEDED)
                        .setWriteDisposition(JobInfo.WriteDisposition.WRITE_APPEND)
                        .build()

        Job job = bigquery.create(JobInfo.of(configuration))
        job = job.waitFor()
        JobStatistics.LoadStatistics stats = job.getStatistics()
        return stats
    }


    /**
     * The Gradle Custom Task @TaskAction.
     */
    @TaskAction
    def taskAction() {
        createDataset(dataset)
        createBucket(bucket)
        analyticsFiles.each { file ->
            uploadFile(bucket, file, "${getBucketPath(file)}")
            loadTable(getEntityName(file, joiner), "gs://${bucket}/${getBucketPath(file)}")
        }
        logSink()
    }
}