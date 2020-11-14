package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import java.io.File

class MavenInfo {
    var mavenUrl: String? = null
    var groupId: String? = null
    var artifactId: String? = null
    var mavenUser: String? = null
    var mavenPsw: String? = null
    var version: String? = null
    var hash: String? = null
    var commitTime: String? = null
    var message: String? = null

    var artifactFile: File? = null

    fun getLog() = "${Keys.PREFIX_LOG}?hash=${hash}&commitTime=${commitTime}&message=${message}}"
}