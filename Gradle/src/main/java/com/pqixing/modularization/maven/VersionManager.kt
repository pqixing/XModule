package com.pqixing.modularization.maven

import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerExtends
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.wrapper.MavenMetadata
import com.pqixing.modularization.wrapper.XmlHelper
import com.pqixing.tools.PropertiesUtils
import org.eclipse.jgit.api.Git
import java.io.File
import java.net.URL

object VersionManager {

    /**
     * 当前最新的版本信息
     */
    private val curVersions = HashMap<String, String>()

    /**
     * 分支相关版本号信息
     */
    private val branchVersion = HashMap<String, HashMap<String, String>>()

    /**
     * 强制指定的版本号信息，分支默认会使用指定的分支信息
     */
    private val targetVersion = HashMap<String, String>()

    /**
     * 按照顺序，查取模块的版本号信息
     * 指定版本 > 分支版本 > 当前版本
     */
    fun getVersion(module: String, branch: String): String {
        checkInit(branch)
        return targetVersion[module] ?: branchVersion[branch]!![module] ?: curVersions[module]
        ?: "+"
    }

    //
    private fun checkInit(branch: String) {
        if (curVersions.isEmpty()) readCurVersions()
        if (branchVersion[branch] == null) readBranchVersion(branch)
        if (targetVersion.isEmpty()) readTargetVersions()
    }

    /**
     * 读取某个分支指定的配置文件
     */
    private fun readBranchVersion(branch: String) {
        val branchMap = HashMap<String, String>()
        branchVersion[branch] = branchMap
        val branchFile = File(FileManager.docRoot, "versions/version_$branch.properties")
        PropertiesUtils.readProperties(branchFile).forEach {
            branchMap[it.key.toString()] = it.value.toString()
        }
    }

    /**
     * 读取指定的版本文件，优先级最高
     */
    private fun readTargetVersions() {
        targetVersion[FileNames.MODULARIZATION] = FileNames.MODULARIZATION
        val info = BasePlugin.getPlugin(ManagerPlugin::class.java)!!.projectInfo
        PropertiesUtils.readProperties(File(info.versionFile)).forEach {
            targetVersion[it.key.toString()] = it.value.toString()
        }
    }

    private fun readCurVersions() {
        curVersions[FileNames.MODULARIZATION] = FileNames.MODULARIZATION
        val baseVersion = File(FileManager.docRoot, "versions/version.properties")
        if (baseVersion.exists()) PropertiesUtils.readProperties(baseVersion).forEach {
            curVersions[it.key.toString()] = it.value.toString()
        } else {
            //从网络处理花版本号
            indexVersionFromNet(baseVersion, curVersions)
        }
        val cacheVersion = File(FileManager.cacheRoot, "versions/${curVersions[Keys.UPDATE_TIME]}.properties")
        indexCacheVersion(cacheVersion, curVersions)
    }

    /**
     * 从本地加载缓存的版本号信息
     */
    private fun indexCacheVersion(cacheVersion: File, curVersions: HashMap<String, String>) {
        val properties = PropertiesUtils.readProperties(cacheVersion)
        val lastUpdate = properties[Keys.UPDATE_TIME] ?: curVersions[Keys.UPDATE_TIME] ?: ""
        val gitInfo = FileManager.docProject?.gitInfo!!

        //cacheMap中的最后更新时间与git版本号的最后更新时间不一致，尝试更新
        if (gitInfo.lastTime != lastUpdate) {
            /**从日志中读取版本号**/
            properties[Keys.UPDATE_TIME] = gitInfo.lastTime
            PropertiesUtils.writeProperties(cacheVersion, properties)
        }
        //加载本地缓存版本号到内存
        properties.forEach {
            curVersions[it.key.toString()] = it.value.toString()
        }
    }

    /**
     * 从网络获取最新的版本号信息
     */
    fun indexVersionFromNet(outFile: File, versions: HashMap<String, String>) {
        val plugin = BasePlugin.getPlugin(ManagerPlugin::class.java)!!
        val extends = plugin.getExtends(ManagerExtends::class.java)
        val maven = extends.groupMaven
        val groupUrl = extends.groupName.replace(".", "/")
        parseNetVersions("$maven/$groupUrl", versions)

        versions[Keys.UPDATE_TIME] = FileManager.docProject?.gitInfo?.lastTime ?: System.currentTimeMillis().toString()
        PropertiesUtils.writeProperties(outFile, versions.toProperties())

        //上传版本好到服务端
        val git = Git.open(FileManager.docRoot)
        git.pull().call()
        git.add().addFilepattern("*").call()
        git.commit().setMessage("indexVersionFromNet ${System.currentTimeMillis()}").call()
        git.push().setForce(true).call()
        git.close()
    }

    /**
     * 解析maven仓库，爬取当前group的所有版本
     */
    fun parseNetVersions(baseUrl: String, versions: HashMap<String, String>) {
        val prefix = "<a href=\""
        val r = Regex(".*?$prefix$baseUrl.*?/</a>")
        URL(baseUrl).readText().lines().forEach { line ->
            if (line.trim().matches(r)) {
                val start = line.indexOf(prefix) + prefix.length
                val metaUrl = line.substring(start, line.indexOf("\">", start)) + FileNames.MAVEN_METADATA
                val meta = MavenMetadata(baseUrl)
                XmlHelper.parseMetadata(URL(metaUrl).readText(), meta)

                versions[meta.artifactId] = meta.release
            }
        }
    }
}