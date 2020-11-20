package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.utils.GitUtils
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.net.URI

class MavenModel {
    var mavenUrl: String = ""
    var groupId: String = ""
    var artifactId: String = ""
    var mavenUser: String = ""
    var mavenPsw: String = ""
        get() = GitUtils.getPsw(field)

    var version: String = ""
    var uploadTask: String = "uploadArchives"
    var lastRev: RevCommit? = null
    var artifactFile: File? = null

    fun mavenUri(): URI = URI(mavenUrl)

    fun getLog() = "${Keys.PREFIX_LOG}?hash=${lastRev?.name}&commitTime=${lastRev?.commitTime}&message=${lastRev?.fullMessage}}"
}