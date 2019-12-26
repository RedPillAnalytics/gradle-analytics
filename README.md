# Introduction
Gradle Analytics generates JSON data files about Gradle activity in the `build/analytics` directory (by default). Custom pipelines can be built for processing these data files. Gradle Analytics also provides capabilities to write the data in these generated files to a few known target locations, called *sinks*.

Read the [API docs](https://s3.amazonaws.com/documentation.redpillanalytics.com/gradle-analytics/latest/index.html).

# Setup

```groovy
plugins {
   id 'com.redpillanalytics.gradle-analytics:[version-number]'
}

analytics {
   ignoreErrors = false
   kafka {
      prod {
         bootstrapServers = 'localhost:9092'
         schemaRegistry = 'http://192.168.1.35:8081'
         acks = 'all'
      }
   }
   s3 {
      dev {
         suffix = 'devops'
      }
   }
   gcs {
      prod {
         prefix = 'rpa-gradle'
      }     
   }
}
```

```shell script
gradle tasks

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Analytics tasks
---------------
gcsProdSink - Process data files using the 'prod' delivery sink and 'rpa-google' naming prefix.
kafkaProdSink - Process data files using the 'prod' delivery sink and 'gradle' naming prefix.
producer - Analytics workflow task for producing data to all configured sinks.
s3DevSink - Process data files using the 'dev' delivery sink and 'gradle' naming prefix.
```

```shell script
gradle producer

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
