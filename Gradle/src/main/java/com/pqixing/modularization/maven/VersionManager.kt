package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.git.PercentProgress
import com.pqixing.git.execute
import com.pqixing.git.init
import com.pqixing.help.MavenMetadata
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerExtends
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.PropertiesUtils
import com.pqixing.tools.UrlUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PushCommand
import java.io.File
import java.net.URL
import java.util.*

object VersionManager {


    private val matchingFallbacks = mutableListOf<String>()
    private var groupName = ""

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
    fun getVersion(branch: String, module: String, version: String): String {
        for (i in -1 until matchingFallbacks.size) {
            val b = if (i < 0) branch else matchingFallbacks[i]
            val branchVersion = findBranchVersion(b)
            val key = "$groupName.$b.$module.$version"
            val v = branchVersion[key] ?: continue
            return "$version.$v"
        }
        return "$version.+"
    }

    //
    @Synchronized
    private fun findBranchVersion(branch: String): HashMap<String, String> {
        if (curVersions.isEmpty()) readCurVersions()
        if (targetVersion.isEmpty()) readTargetVersions()
        return branchVersion[branch] ?: readBranchVersion(branch)
    }

    /**
     * 读取某个分支指定的配置文件
     */
    private fun readBranchVersion(branch: String): HashMap<String, String> {
        val branchMap = HashMap<String, String>()
        branchVersion[branch] = branchMap
        //将基础版本放入
        branchMap.putAll(curVersions)
        val branchFile = File(FileManager.docRoot, "versions/version_$branch.properties")
        PropertiesUtils.readProperties(branchFile).forEach {
            branchMap[it.key.toString()] = it.value.toString()
        }
        //最后放入指定版本
        branchMap.putAll(targetVersion)
        return branchMap
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
        BasePlugin.getPlugin(ManagerPlugin::class.java)?.getExtends(ManagerExtends::class.java)?.apply {
            this@VersionManager.matchingFallbacks.addAll(matchingFallbacks)
            this@VersionManager.groupName = groupName
        }
        curVersions[FileNames.MODULARIZATION] = FileNames.MODULARIZATION

        val baseVersion = File(FileManager.infoDir, "versions/version.properties")
        val basePros = PropertiesUtils.readProperties(baseVersion)
        if (basePros.isNotEmpty()) PropertiesUtils.readProperties(baseVersion).forEach {
            curVersions[it.key.toString()] = it.value.toString()
        } else {
            //从网络处理花版本号
            indexVersionFromNet(baseVersion, curVersions)
        }
        val updateTime = curVersions[Keys.UPDATE_TIME]?.toInt()
                ?: (System.currentTimeMillis() / 1000).toInt()
        indexCacheVersion(updateTime, curVersions)
    }

    /**
     * 从本地加载缓存的版本号信息
     */
    private fun indexCacheVersion(lastUpdate: Int, curVersions: HashMap<String, String>) {
        val lastLog = FileManager.docProject.lastLog
        //cacheMap中的最后更新时间与git版本号的最后更新时间不一致，尝试更新
        if (lastLog.commitTime != lastUpdate) {
            /**从日志中读取版本号**/
            val git = Git.open(FileManager.docRoot)
            run out@{
                git.log().call().forEach { rev ->
                    if (rev.commitTime < lastUpdate) return@out
                    val message = rev.fullMessage.trim()
                    //如果是
                    if (!message.startsWith(Keys.PREFIX_TO_MAVEN)) return@forEach

                    val params = UrlUtils.getParams(message)
                    if (params == null || params.size < 3) return@forEach
                    addVersion(curVersions, params[Keys.LOG_BRANCH]!!, params[Keys.LOG_MODULE]!!, listOf(params[Keys.LOG_VERSION]!!))
                }
            }
        }
    }

    /**
     * 把每个版本的最后版本号添加
     */
    private fun addVersion(curVersions: HashMap<String, String>, groupId: String, artifactId: String, version: List<String>) {
        //倒叙查询
        for (i in version.size - 1 downTo 0) {
            val v = version[i]
            val l = v.lastIndexOf('.')
            val bv = v.substring(0, l)
            val lv = v.substring(l + 1)
            val key = "$groupId.$artifactId.$bv"
            val latKey = curVersions[key]?.toInt() ?: 0
            if (lv.toInt() > latKey) {
                curVersions[key] = lv
                Tools.println("addVersion -> $groupId -> $artifactId -> $v")
            }
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
        Tools.println("indexVersionFromNet  start ->")
        parseNetVersions("$maven/$groupUrl", versions, extends.groupName)
        Tools.println("indexVersionFromNet  end ->")
        versions[Keys.UPDATE_TIME] = (System.currentTimeMillis() / 1000).toInt().toString()
        //上传版本好到服务端
        val git = Git.open(FileManager.docRoot)
        git.pull().setCredentialsProvider(FileManager.docCredentials).setProgressMonitor(PercentProgress()).call()

        PropertiesUtils.writeProperties(outFile, versions.toProperties())
        Tools.println("indexVersionFromNet update from net save to -> $outFile")

        git.add().addFilepattern(FileNames.PROJECTINFO).init().execute()
        git.commit().setAllowEmpty(true).setMessage("indexVersionFromNet ${Date().toLocaleString()}").init().execute()
        (git.push().init() as PushCommand).setCredentialsProvider(FileManager.docCredentials).setForce(true).execute()
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
                        addVersion(versions, meta.groupId.trim(), meta.artifactId.trim(), meta.versions)
                        return@outer
                    } else {
                        parseNetVersions(url, versions, groupName)
                    }
                }
            }
        }
    }
}
