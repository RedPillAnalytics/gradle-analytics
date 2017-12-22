package common

import groovy.util.logging.Slf4j
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

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
}
