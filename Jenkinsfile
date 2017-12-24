def options = '-Si'
def properties = "-PbuildId=${env.BUILD_TAG}"
def gradle = "./gradlew ${options} ${properties}"

pipeline {
   agent { label 'java-compile' }

   environment {
      GOOGLE_APPLICATION_CREDENTIALS = '/var/lib/jenkins/.gcp/gradle-analytics.json'
   }

   stages {

      stage('Prepare') {
         when { branch 'master' }
         steps {
            sh "$gradle clean"
         }
      }

      stage('Release') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} release -Prelease.disableChecks -Prelease.localOnly"
         }
      }

      stage('Build') {
         steps {
            sh "$gradle build"
         }
      }

      stage('Integration') {
         steps {
            sh "$gradle integrationTest --rerun-tasks"
         }
      }

      stage('Publish') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} githubRelease publishPlugins"
            sh "$gradle uploadGroovydoc"
         }
      }
      // Place for new Stage

   } // end of Stages

   post {
      always {
         junit "build/test-results/**/*.xml"
         archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
         sh "$gradle uploadGroovydoc"
      }
   }

}
