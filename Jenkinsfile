def options = '-Si'
def properties = "-PbuildId=${env.BUILD_TAG}"
def gradle = "./gradlew ${options} ${properties}"

pipeline {
   agent { label 'java-compile' }

   environment {
      ANALYTICS = credentials('services-analytics-user')
   }

   stages {

      stage('Release') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} release -Prelease.disableChecks -Prelease.localOnly"
         }
      }

      stage('Build') {
         steps {
            sh "$gradle clean build"
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
