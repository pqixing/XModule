package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.git.PercentProgress
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
import java.util.*

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
        val key = (if (branch.isEmpty()) "" else ".$branch") + ".$module"
        return targetVersion[key] ?: branchVersion[branch]!![key] ?: curVersions[key]
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
        PropertiesUtils.readProperties(File(info?.versionFile)).forEach {
            targetVersion[it.key.toString()] = it.value.toString()
        }
    }

    private fun readCurVersions() {
        curVersions[FileNames.MODULARIZATION] = FileNames.MODULARIZATION
        val baseVersion = File(FileManager.infoDir, "versions/version.properties")
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
        val lastUpdate = (properties[Keys.UPDATE_TIME] ?: curVersions[Keys.UPDATE_TIME]
        ?: "0").toString()
        val lastLog = FileManager.docProject.lastLog

        //cacheMap中的最后更新时间与git版本号的最后更新时间不一致，尝试更新
        if (lastLog.commitTime != lastUpdate) {
            /**从日志中读取版本号**/
            val git = Git.open(FileManager.docRoot)
            val lastTime = lastUpdate.toInt()
            run out@{
                git.log().call().forEach { rev ->
                    if (rev.commitTime < lastTime) return@out
                    val message = rev.fullMessage.trim()
                    //如果是
                    if (!message.startsWith(Keys.PREFIX_TO_MAVEN)) return@forEach

                    val list = message.split(":")
                    if (list.size < 3) return@forEach
                    curVersions[list[1]] = list[2]
                }
            }
            properties[Keys.UPDATE_TIME] = lastLog.commitTime
            PropertiesUtils.writeProperties(cacheVersion, properties)
        }
        //加载本地缓存版本号到内存
        properties.forEach {
            curVersions[it.key.toString()] = it.value.toString()
        }
    }

    fun indexVersionFromNet() {
        curVersions.clear()
        indexVersionFromNet(File(FileManager.infoDir, "versions/version.properties"), curVersions)
    }

    /**
     * 从网络获取最新的版本号信息
     */
    fun indexVersionFromNet(outFile: File, versions: HashMap<String, String>) {
        val plugin = BasePlugin.getPlugin(ManagerPlugin::class.java)!!
        val extends = plugin.getExtends(ManagerExtends::class.java)
        val maven = extends.groupMaven
        val groupUrl = extends.groupName.replace(".", "/")
        parseNetVersions("$maven/$groupUrl", versions, extends.groupName)

        versions[Keys.UPDATE_TIME] = FileManager.docProject.lastLog.commitTime
        PropertiesUtils.writeProperties(outFile, versions.toProperties())
        Tools.println("indexVersionFromNet update from net save to -> $outFile")
        //上传版本好到服务端
        val git = Git.open(FileManager.docRoot)
        git.pull().setCredentialsProvider(FileManager.docCredentials).setProgressMonitor(PercentProgress()).call()
        git.add().addFilepattern(".").call()
        git.commit().setAllowEmpty(true).setMessage("indexVersionFromNet ${Date().toLocaleString()}").call()
        git.push().setCredentialsProvider(FileManager.docCredentials).setForce(true).setProgressMonitor(PercentProgress()).call()
        git.close()
    }

    /**
     * 解析maven仓库，爬取当前group的所有版本
     */
    fun parseNetVersions(baseUrl: String, versions: HashMap<String, String>, groupName: String) {
        val prefix = "<a href=\""
        val r = Regex(".*?$prefix$baseUrl.*?</a>")
        val lines = URL(baseUrl).readText().lines()
        kotlin.run outer@{
            lines.forEach { line ->
                if (line.trim().matches(r)) {
                    val start = line.indexOf(prefix) + prefix.length
                    val url = line.substring(start, line.indexOf("\">", start))
                    if (url.endsWith(FileNames.MAVEN_METADATA)) {
                        val meta = MavenMetadata(baseUrl)
                        XmlHelper.parseMetadata(URL(url).readText(), meta)
                        versions["${meta.groupId.replace(groupName, "")}.${meta.artifactId}"] = meta.release
                        return@outer
                    } else {
                        parseNetVersions(url, versions, groupName)
                    }
                }
            }
        }
    }
}
