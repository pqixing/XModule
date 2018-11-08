package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerExtends
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.net.Net
import java.io.File
import java.util.*

object VersionManager {

    /**
     * 当前最新的版本信息
     */
    private val curVersions = HashMap<String, String>()

    /**
     * 强制指定的版本号信息，分支默认会使用指定的分支信息
     */
    private val targetVersion = HashMap<String, String>()
    private var hasInit = false

    fun getVersion(module: String, branch: String): String {
        checkInit()
        return ""
    }

    private fun checkInit() {
        if (hasInit) return
        readCurVersions()
        readTargetVersions()
        hasInit = true
    }

    private fun readTargetVersions() {

    }

    private fun readCurVersions() {
        val versionFile = File(FileManager.docRoot, "versions/version.properties")
        if (!versionFile.exists()) {

        }
    }

    /**
     * 从网络获取最新的版本号信息
     */
    fun indexVersionFromNet() {
        val plugin = BasePlugin.getPlugin(ManagerPlugin::class.java)
        val extends = plugin.getExtends(ManagerExtends::class.java)
        val maven = extends.groupMaven
        val groupUrl = extends.groupName.replace(".", "/")
        parseArtifactId(maven, groupUrl)
    }

    fun parseArtifactId(maven: String, groupUrl: String) {
        val url = "$maven/$groupUrl/"
        val prefix = "<a href=\""
        val r = Regex("$prefix${url}.*?/</a>")
        Net.get("$maven/$groupUrl/").lines().forEach { line ->
            if (line.matches(r)) {
                val start = line.indexOf(prefix) + prefix.length + 1
                val metaUrl = line.substring(start, line.indexOf("\">", start)) + "maven-metadata.xml"
                System.out.println(metaUrl)
            }
        }
    }
}
