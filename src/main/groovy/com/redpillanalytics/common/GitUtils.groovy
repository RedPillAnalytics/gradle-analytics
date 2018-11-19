package com.redpillanalytics.common

import groovy.util.logging.Slf4j
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

/**
 * Created by stewart on 5/2/16.
 */

@Slf4j
class GitUtils {

    FileRepository repository
    Git git
    String initialBranch
    String initialFullBranch
    String remoteUrl
    String emailAddress


    GitUtils(String filePath = '.') {

        try {

            def repoFile = new File(filePath + '/.git')
            // construct a repo object
            repository = new FileRepositoryBuilder()
                    .setGitDir(repoFile)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build()

            log.debug repository.dump()

            def head = repository.findRef("HEAD")
            def walk = new RevWalk(repository)
            def commit = walk.parseCommit(head.getObjectId())

            def authorIdent = commit.getAuthorIdent()

            git = new Git(repository)

            this.initialBranch = getCurrentBranch()

            this.initialFullBranch = getCurrentFullBranch()

            this.remoteUrl = git.repository.getConfig().getString('remote', 'origin', 'url')

            this.emailAddress = authorIdent.emailAddress ?: ""


        } catch (Exception e) {

            log.info "Not executing from a Git repository"
            // just pass it through
        }
    }

    def getCurrentBranch() {

        return repository.getBranch()
    }

    def getCurrentFullBranch() {

        def branch = repository.getFullBranch()

        log.debug "Initial full branch: ${branch}"

        return branch

    }

    def getCommitHash() {

        def hash = ""

        try {
            hash = repository.findRef('HEAD').getObjectId().getName()
        } catch(NullPointerException ex) {
            log.info(ex.toString())
        }

        return hash
    }

    def checkoutBranch(String branch) {

        git.checkout().setName(branch).call()

    }

    def checkoutInitialBranch(Boolean ignore = false) {

        checkoutBranch(initialBranch)
    }

    def mergeBranch(String branch, Boolean ignore = false) {

        git.merge().include(repository.findRef(branch))

    }

    def mergeInitialBranch(Boolean ignore = false) {

        mergeBranch(initialBranch, ignore)
    }

    def checkoutFile(String treeish, File source, Boolean reset = false, Boolean ignore = false) {

        // checkout an individual file or directory
        // this is a "mixed" checkout... we get portions of different branches together
        // this actually works like a merge between branches... not a replace
        git.checkout().setStartPoint(treeish).setForce(true)
        git.checkout().addPath(source).call()

    }

    def addFile(File file) {

        addFile(file.getPath())
    }

    def addFile(String pattern = '.') {

        git.add().addFilepattern(pattern).call()
    }

    def commit(String message, String author = null, String email = null, String path = null) {

        git.commit().setMessage(message).setAmend(true)

        if (path) {

            git.commit().setOnly(path)
        }

        if (author || email) {

            git.commit().setAuthor(author, email).setMessage(message).call()

        } else {

            git.commit().setMessage(message).call()
        }
    }

    def resetFile(File file) {

        log.debug "Resetting file: ${file}"
        git.reset().addPath(file.canonicalPath).call()
        git.checkout().addPath(file.canonicalPath).call()
    }

    def push(String username, String password) {
        def branch = repository.getBranch()
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setRemote("origin").setRefSpecs(new RefSpec(branch + ":" + branch)).call()
    }
}
