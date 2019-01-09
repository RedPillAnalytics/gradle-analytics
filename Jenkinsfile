def options = '-Si'
def properties = "-Panalytics.buildId=${env.BUILD_TAG}"
def gradle = "./gradlew ${options} ${properties}"

pipeline {
   agent { label 'java-compile' }

   environment {
      GOOGLE_APPLICATION_CREDENTIALS = '/var/lib/jenkins/.gcp/gradle-analytics-build-user.json'
   }

   stages {

      stage('Release') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} clean release -Prelease.disableChecks -Prelease.localOnly"
         }
      }

      stage('Build') {
         steps {
            sh "$gradle build"
         }
      }

      stage('Integration') {
         steps {
            sh "$gradle amazonTest googleTest --rerun-tasks"
         }
      }

      stage('Publish') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} publishPlugins uploadGroovydoc githubRelease"
         }
      }
      // Place for new Stage

   } // end of Stages

   post {
      always {
         junit "build/test-results/**/*.xml"
         archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
         //sh "$gradle producer"
      }
   }

}
