package common

/**
 * Created by stewartbryson on 11/18/16.
 */
class CI {

   static gitUtils = new GitUtils()


   static getBuildParameter(String varName) {

      return System.getenv(varName) ?: System.getenv('bamboo_' + varName)

   }

   static getBuildNumber() {

      return System.getenv('SOURCE_BUILD_NUMBER') ?: System.getenv('bamboo_buildNumber') ?: getTimestamp()

   }

   static getBuildNumExt() {

      return '.' + getBuildNumber()

   }

   static getBuildTag() {

      return System.getenv('BUILD_TAG') ?: System.getenv('bamboo_buildResultKey') ?: getTimestamp()

   }

   static getBuildTagExt() {

      '-' + getBuildTag()

   }

   static getBuildUrl() {

      return System.getenv('BUILD_URL') ?: System.getenv('bamboo_resultsUrl')

   }

   static isJenkinsCI() {

      if (getCIServer() == 'jenkins')

         return true
      else

         return false
   }

   static isBambooCI() {

      if (getCIServer() == 'bamboo')

         return true
      else

         return false
   }

   static getCIServer() {

      if (System.getenv('JENKINS_HOME')) {

         return 'jenkins'
      } else if (System.getenv('bamboo_planKey')) {

         return 'bamboo'
      } else {

         return 'other'
      }
   }

   static getRepositoryUrl() {

      return System.getenv('GIT_TAG') ?: System.getenv('bamboo_planRepository_repositoryUrl') ?: gitUtils.remoteUrl ?: ""
   }

   static getGitHubOrg() {

      getRepositoryUrl().find(/(\/|:)(.+)(\/)([^.]+)/) { all, firstSlash, org, secondSlash, repo ->

         return org.toString()
      }
   }

   static getGitHubRepo() {

      getRepositoryUrl().find(/(\/|:)(.+)(\/)([^.]+)/) { all, firstSlash, org, secondSlash, repo ->

         return repo.toString()
      }
   }

   static getBranch() {

      return System.getenv('GIT_LOCAL_BRANCH') ?: System.getenv('bamboo_planRepository_branchName') ?: gitUtils.initialBranch
   }

   static getCommitEmail() {

      return gitUtils.emailAddress
   }

   static getCommitHash() {

      return gitUtils.getCommitHash()
   }

   static generateBuildId() {

      return UUID.randomUUID().toString()
   }

   static getTimestamp() {

      return new Date().format('yyyy-MM-dd-HHmmssSS')
   }
}
