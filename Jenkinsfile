def options = '-Si'
def properties = "-Panalytics.buildId=${env.BUILD_TAG}"
def gradle = "./gradlew ${options} ${properties}"

pipeline {
   agent { label 'java-compile' }

   environment {
      GOOGLE_APPLICATION_CREDENTIALS = './gradle-analytics-build-user.json'
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
            sh "$gradle cleanTests build copyBuildResources cV"
            junit testResults: "build/test-results/test/*.xml", allowEmptyResults: true, keepLongStdio: true
         }
      }

      stage('Integration') {
          steps {
              sh "$gradle composeUp"
              sleep 5
              sh "$gradle integrationStage --rerun-tasks"
              junit testResults: "build/test-results/*Test/*.xml", allowEmptyResults: true, keepLongStdio: true
          }
      }

      stage('Publish') {
         when { branch "master" }
         steps {
            sh "$gradle ${options} publishPlugins githubRelease"
         }
      }
      // Place for new Stage

   } // end of Stages

   post {
      always {
         archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
         //sh "$gradle producer"
      }
      cleanup {
        sh "$gradle composeDown"
      }
   }

}
