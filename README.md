# Introduction
Gradle Analytics generates JSON data files about Gradle activity in the `build/analytics` directory (by default). Custom pipelines can be built for processing these data files, but Gradle Analytics provides capabilities to write the data from these generated files to a few known target locations, called *sinks*.

In most cases, Gradle Analytics tries to create destination buckets, datasets, etc. An exception is Kinesis Firehose, as the setup is complicated.

Read the [API docs](https://s3.amazonaws.com/documentation.redpillanalytics.com/gradle-analytics/latest/index.html).

# Setup

```groovy
plugins {
   id 'com.redpillanalytics.gradle-analytics'
}

analytics {
   ignoreErrors = false

   // write to Google BigQuery
   bq {
      // can configure multiple locations or "environments"
      // used in generating the task name
      test {
         // Files are first staged in a GCS bucket
         bucket = 'rpa-gradle'
         // then they are loaded to a BigQuery dataset
         dataset = 'gradle_analytics'
      }     
   }

   // write to a Kafka cluster
   kafka {
      prod {
         bootstrapServers = 'localhost:9092'
         schemaRegistry = 'http://192.168.1.35:8081'
         acks = 'all'
      }
   }
   // write to an S3 bucket
   s3 {
      dev {
         // the bucket name to write to
         bucket = 'rpa-gradle'
        // a suffix to add to the end of the default entity names
        suffix = 'dev'
      }
   }
   // write to a Google Cloud Storage bucket
   gcs {
      prod {
         // the bucket name to write to
         bucket = 'rpa-gradle'
         // a prefix to add to the beginning of the default entity names
         prefix = 'prod'
      }     
   }
}
```

```shell script
-> gradle tasks --group analytics

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Analytics tasks
---------------
bqProdSink - Process data files using the 'prod' delivery sink. Each generated analytics file is written to a corresponding BigQuery table based on 'dataset', 'suffix' and 'prefix' values.
gcsProdSink - Process data files using the 'prod' delivery sink. Each generated analytics file is written to a corresponding GCS path based on 'suffix' and 'prefix' values.
kafkaProdSink - Process data files using the 'prod' delivery sink. Each generated analytics file is written to a corresponding Kafka topic based on 'suffix' and 'prefix' values.
producer - Analytics workflow task for producing data to all configured sinks.
s3DevSink - Process data files using the 'dev' delivery sink. Each generated analytics file is written to a corresponding S3 path based on 'suffix' and 'prefix' values.
```

```shell script
-> gradle producer

> Task :gcsProdSink
26 analytics files processed.

> Task :kafkaProdSink
26 analytics files processed.

> Task :s3DevSink
26 analytics files processed.

> Task :producer

BUILD SUCCESSFUL in 16s
4 actionable tasks: 4 executed
```

# Better Documentation Coming Soon
