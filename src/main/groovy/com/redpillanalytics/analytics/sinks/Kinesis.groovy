package com.redpillanalytics.analytics.sinks

import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient
import com.amazonaws.services.kinesisfirehose.model.PutRecordBatchRequest
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest
import com.amazonaws.services.kinesisfirehose.model.Record
import groovy.util.logging.Slf4j

import java.nio.ByteBuffer

@Slf4j
@groovy.transform.InheritConstructors
class Kinesis extends Sink {

    def firehose = new AmazonKinesisFirehoseClient()
    def recordList = []
    def recordBatchRequest = new PutRecordBatchRequest()


    def sendRecord(String stream, String data) {

        // define a request
        def request = new PutRecordRequest()

        // define result now
        def result

        try {
            // set the delivery stream
            request.setDeliveryStreamName(stream)

            // instantiate a record type
            def record = new Record()

            record.setData(ByteBuffer.wrap((data).getBytes()))
            request.setRecord(record)

            result = firehose.putRecord(request)

            log.info "Kinesis Firehose record sent to '${stream}'"

            log.debug "Kinesis Firehose record: ${data}"

            log.debug "Kinesis Firehose result: ${result}"

        } catch (Exception e) {

            if (ignoreErrors) {

                log.info e.toString()
            } else {

                throw e
            }
        }

    }

    def sendRecord(String stream, File file) {

        sendRecord(stream, file.text)

    }

    def putRecord(String data) {

        log.debug "raw data dump: ${data.dump()}"

        def record = new Record().withData(ByteBuffer.wrap((data + '\n').getBytes()))

        log.debug "record: ${record}"

        recordList.add(record)
    }

    def setRecordStream(String stream) {

        try {

            recordBatchRequest.setDeliveryStreamName(stream)

        } catch (Exception e) {

            if (ignoreErrors) {

                log.info e.toString()
            } else {

                throw e
            }

        }

        log.info "Kinesis Firehose stream set to '${stream}'"
    }


    def sendRecordBatch() {

        def result

        try {

            recordBatchRequest.setRecords(recordList)

            result = firehose.putRecordBatch(recordBatchRequest)

            recordList.clear()

        } catch (Exception e) {

            if (ignoreErrors) {

                log.info e.toString()
            } else {

                throw e
            }

        }

        log.info "Kinesis Firehose batch sent successfully"

        log.debug "Kinesis Firehose result: ${result}"

    }

}