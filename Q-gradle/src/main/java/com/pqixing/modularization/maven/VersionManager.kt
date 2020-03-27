package com.pqixing.modularization.maven

import com.pqixing.EnvKeys
import com.pqixing.Tools
import com.pqixing.getEnvValue
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.manager.ExceptionManager
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerExtends
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import com.pqixing.tools.TextUtils
import com.pqixing.tools.UrlUtils
import org.eclipse.jgit.api.Git
import java.io.File
import java.net.URL
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.collections.HashSet

object VersionManager : OnClear {
    val matchingFallbacks get() = ManagerPlugin.getExtends().matchingFallbacks.toMutableList()
    val groupName get() = ManagerPlugin.getExtends().groupName

    init {
        BasePlugin.addClearLister(this)
    }

    override fun start() {
    }

    override fun clear() {
        curVersions.clear()
        targetVersion.clear()
        branchVersion.clear()
        matchingFallbacks.clear()
        repoLastCommit = 0
    }

    //版本号的管理工程,迁回root目录下,方便调试和查看
    var repoGitDir: File = File(".")
        get() {
            //删除旧的目录
            FileUtils.delete(File(FileManager.rootDir, "build/version"))
            return FileManager.templetRoot
        }

    private var repoLastCommit = 0


    /**
     * 当前最新的版本信息
     */
    val curVersions = HashMap<String, String>()

    /**
     * 分支相关版本号信息
     */
    private val branchVersion = HashMap<String, HashMap<String, String>>()

    /**
     * 强制指定的版本号信息，分支默认会使用指定的分支信息
     */
    private val targetVersion = HashMap<String, String>()

    /**
     * 获取指定分支指定baseVersion版本的最新版本号
     */
    fun getNewerVersion(branch: String, module: String, version: String): Int {
        if (curVersions.isEmpty()) readCurVersions()
        val key = "$groupName.$branch.$module.$version"
        return curVersions[key]?.toInt() ?: -1
    }

    /**
     * 根据分支，查找出所有模块名称
     */
    fun findAllModuleByBranch(branch: String): Set<String> {
        if (curVersions.isEmpty()) readCurVersions()
        val preKey = "$groupName.$branch."
        return curVersions.keys.filter { it.startsWith(preKey) }.map {
            val r = it.replace(preKey, "")
            r.substring(0, r.indexOf("."))
        }.toSet()
    }


    /**
     * 按照顺序，查取模块的版本号信息
     * 指定版本 > 分支版本 > 当前版本
     */
    fun getVersion(branch: String, module: String, inputVersion: String): Pair<String, String> {
        val branchVersion = findBranchVersion(branch)
        val start = matchingFallbacks.indexOf(branch)
        for (i in start until matchingFallbacks.size) {
            val b = if (i < 0) branch else matchingFallbacks[i]
            val preKey = "$groupName.$b.$module."
            var version = inputVersion
            if (TextUtils.isVersionCode(version)) {
                val i1 = version.lastIndexOf('.')
                if (i1 < 0) continue
                val baseVersion = version.substring(0, i1)
                val last = version.substring(i1 + 1).toInt()
                val v = branchVersion["$preKey$baseVersion"]?.toInt() ?: continue
                if (v >= last) return Pair(b, version)//.apply { Tools.println("getVersion -> $branch $module $inputVersion -> $first : $second")  }
            } else {
                version = if (TextUtils.isBaseVersion(inputVersion)) inputVersion else findBaseVersion(inputVersion, preKey, branchVersion)
                //如果传入的是固定的版本号,则只查询各分支是否存在此版本号，不做自动升级版本号处理
                if (TextUtils.isBaseVersion(version)) {
                    val v = branchVersion["$preKey$version"] ?: continue
                    return Pair(b, "$version.$v")//.apply { Tools.println("getVersion -> $branch $module $inputVersion -> $first : $second")  }
                }
            }
        }
        return Pair("", inputVersion)
    }

    /**
     * 检查改分支是否存在版本号
     */
    fun checkBranchVersion(branch: String, module: String): Boolean {
        val branchVersion = findBranchVersion(branch)
        val preKey = "$groupName.$branch.$module."
        return TextUtils.isBaseVersion(findBaseVersion("+", preKey, branchVersion))
    }


    private fun findBaseVersion(v: String, preKey: String, versions: HashMap<String, String>): String {
        var vs = versions.keys.filter { it.startsWith(preKey) }
        if (vs.isEmpty()) return "+"
        vs = vs.map { it.replace(preKey, "") }
                .sortedWith(Comparator { p0, p1 -> -TextUtils.compareVersion(p0, p1) })
        if (v.isEmpty() || v == "+") return vs[0]
        val v1 = "$v."
        for (s in vs) {
            if (s.startsWith(v1)) return s
        }
        return "+"
    }

    fun findBranchVersion(branch: String): HashMap<String, String> {
        if (curVersions.isEmpty()) readCurVersions()
        if (targetVersion.isEmpty()) readTargetVersions()
        return branchVersion[branch] ?: readBranchVersion(branch)
    }

    private fun getBranchVersionName(branch: String): String {
        val of = branch.lastIndexOf("/");
        return "versions/version_${branch.substring(of + 1)}.properties"
    }

    /**
     * 读取某个分支指定的配置文件
     */
    private fun readBranchVersion(branch: String): HashMap<String, String> {
        val branchMap = HashMap<String, String>()
        branchVersion[branch] = branchMap
        //将基础版本放入
        branchMap.putAll(curVersions)
        val branchFile = File(repoGitDir, getBranchVersionName(branch))
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
        val info = ManagerPlugin.getPlugin().config
        PropertiesUtils.readProperties(File(info?.versionFile)).forEach {
            targetVersion[it.key.toString()] = it.value.toString()
        }
    }

    private fun readCurVersions() {
        if (curVersions.isNotEmpty()) return
        prepareVersions()
        curVersions[FileNames.MODULARIZATION] = FileNames.MODULARIZATION

        val baseVersion = File(repoGitDir, "versions/version.properties")
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
     * 检查version Git的状态
     */
    private fun prepareVersions() {
        val extends = ManagerPlugin.getExtends()
        val vBranch = "_v2"
        val git = GitUtils.open(repoGitDir)
                ?: GitUtils.clone(extends.docRepoUrl, repoGitDir, vBranch)
        if (git == null) {
            ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "can not find version repo!!")
            return
        }
        //如果是ToMaven,则更新,否则,不需要每次都更新
        if (ManagerPlugin.getPlugin().runTaskNames.toString().contains("ToMaven")) git.pull()
//        if (!GitUtils.checkoutBranch(git, vBranch, true)) {
//            if (!GitUtils.createBranch(git, vBranch)) {
//                ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "can checkout to branch : $vBranch")
//            }
//        }
        repoLastCommit = git.log().setMaxCount(1).call().firstOrNull()?.commitTime
                ?: (System.currentTimeMillis() / 1000).toInt()
        GitUtils.close(git)
    }

    /**
     * 从本地加载缓存的版本号信息
     */
    private fun indexCacheVersion(lastUpdate: Int, curVersions: HashMap<String, String>) {
        //cacheMap中的最后更新时间与git版本号的最后更新时间不一致，尝试更新
        if (repoLastCommit > lastUpdate) {
            /**从日志中读取版本号**/
            val git = Git.open(repoGitDir)
            run out@{
                git.log().call().forEach { rev ->
                    //如果当前记录比最后更新时间小，则无需再更新，因为已经在version里面了
                    if (rev.commitTime < lastUpdate) return@out
                    val message = rev.fullMessage.trim()
                    //如果是
                    if (!message.startsWith(Keys.PREFIX_TO_MAVEN)) return@forEach
                    val params = UrlUtils.getParams(message)
                    if (params == null || params.size < 3) return@forEach
                    addVersion(curVersions, "$groupName.${params[Keys.LOG_BRANCH]}", params[Keys.LOG_MODULE]!!, listOf(params[Keys.LOG_VERSION]!!))
                }
            }
            GitUtils.close(git)
        }
    }

    /**
     * 把每个版本的最后版本号添加
     */
    private fun addVersion(curVersions: HashMap<String, String>, groupId: String, artifactId: String, version: List<String>) {
        val addV = StringBuffer()
        //倒叙查询
        for (i in version.size - 1 downTo 0) {
            val v = version[i]
            val l = v.lastIndexOf('.')
            val bv = v.substring(0, l)
            val lv = v.substring(l + 1)
            val key = "$groupId.$artifactId.$bv"
            val latKey = curVersions[key]?.toInt() ?: -1
            if (lv.toInt() > latKey) {
                curVersions[key] = lv
                addV.append(v).append(",")
            }
        }
    }

    fun indexVersionFromNet() {
        curVersions.clear()
        branchVersion.clear()
        indexVersionFromNet(File(repoGitDir, "versions/version.properties"), curVersions)
    }

    /**
     * 创建一个分支的版本号Tag标签
     */
    fun createVersionTag(): Boolean {
        val plugin = ManagerPlugin.getPlugin()
        val opBranch = EnvKeys.opBranch.getEnvValue()?:return false
        val taskBranch = opBranch.substring(opBranch.lastIndexOf("/") + 1)//直接获取名称,不要origin
        if (taskBranch.isEmpty() || taskBranch == "master") {
            Tools.printError(-1, "createVersionTag taskBranch is empty, please input taskBranch!!")
            return false
        }
        //拷贝一份
        val fallbacks = matchingFallbacks.toMutableList()
        (EnvKeys.tagBranch.getEnvValue()?:"").split(",").map { it.split("/").last().trim() }.forEach { if (it.isNotEmpty() && !fallbacks.contains(it)) fallbacks.add(it) }

        val matchKeys = fallbacks.map { "$groupName.$it." }
        val tagVersions = curVersions.filter { c -> matchKeys.any { f -> c.key.startsWith(f) } }

        val branchFile = File(repoGitDir, getBranchVersionName(taskBranch))
        PropertiesUtils.writeProperties(branchFile, tagVersions.toProperties())
        ResultUtils.writeResult(branchFile.absolutePath)
        val git = Git.open(repoGitDir)
        GitUtils.addAndPush(git, "versions", "createVersionTag $taskBranch ${DateFormat.getDateTimeInstance().format(Date())}", true)
        GitUtils.close(git)
        return true
    }

    /**
     * 从网络获取最新的版本号信息
     */
    private fun indexVersionFromNet(outFile: File, versions: HashMap<String, String>) {
        if (!GitUtils.isGitDir(repoGitDir)) prepareVersions()
        val plugin = ManagerPlugin.getPlugin()
        val extends = plugin.getExtends(ManagerExtends::class.java)
        val maven = extends.groupMavenView
        val groupUrl = extends.groupName.replace(".", "/")
        Tools.println("parseNetVersions  start -> $groupUrl")
        val start = System.currentTimeMillis()
        versions.clear()
        if (!maven.startsWith("http")) {
            parseLocalVersion(File(maven, groupUrl), versions)
        } else parseNetVersions(getFullUrl(groupUrl, maven), versions, extends.groupName)
        Tools.println("parseNetVersions  end -> ${System.currentTimeMillis() - start} ms")
        versions[Keys.UPDATE_TIME] = (System.currentTimeMillis() / 1000).toInt().toString()
        //上传版本好到服务端
        val git = Git.open(repoGitDir)
        GitUtils.pull(git)

        PropertiesUtils.writeProperties(outFile, versions.toProperties())
        GitUtils.addAndPush(git, "versions", "indexVersionFromNet ${DateFormat.getDateTimeInstance().format(Date())}", false)
        GitUtils.close(git)
    }

    /**
     * 解析本地仓库的版本信息
     */
    private fun parseLocalVersion(dir: File, versions: HashMap<String, String>) {
        if (!dir.exists() || dir.isFile) return

        for (f in dir.listFiles { f -> f.isFile }) {
            if (f.name == "maven-metadata.xml") {//解析版本,如果已经找到了版本, 则不再需要
                val meta = XmlHelper.parseMetadata(FileUtils.readText(f) ?: "")
                addVersion(versions, meta.groupId.trim(), meta.artifactId.trim(), meta.versions)
                return
            }
        }
        //递归解析本地目录
        dir.listFiles { f -> f.isDirectory }.forEach { parseLocalVersion(it, versions) }
    }

    /**
     *
     */
    fun readNetUrl(url: String) = try {
        URL(url).readText()
    } catch (e: Exception) {
        ""
    }

    fun getFullUrl(url: String, baseUrl: String): String {
        if (url == "..") return "";
        if (url.startsWith("http:")) return url
        if (baseUrl.endsWith("/")) return "$baseUrl$url"
        return "$baseUrl/$url"
    }

    /**
     * 解析maven仓库，爬取当前group的所有版本
     */
    fun parseNetVersions(baseUrl: String, versions: HashMap<String, String>, groupName: String, readUrls: HashSet<String> = HashSet()) {
        if (readUrls.contains(baseUrl)) return//防止重复请求处理
        readUrls.add(baseUrl)
        val htmlText = readNetUrl(baseUrl)
        var matcher = Pattern.compile("<a href=.*?>maven-metadata.xml</a>").matcher(htmlText)
        if (matcher.find()) {
            val group = matcher.group()
            val meteUrl = group.substring(group.indexOf('"') + 1, group.lastIndexOf('"'))
            val meta = XmlHelper.parseMetadata(readNetUrl(getFullUrl(meteUrl, baseUrl)))
            addVersion(versions, meta.groupId.trim(), meta.artifactId.trim(), meta.versions)
            return
        }
        //查找相关路径的,爬
        matcher = Pattern.compile("<a href=.*?</a>").matcher(htmlText)
        while (matcher.find()) {
            val group = matcher.group()
            val url = getFullUrl(group.substring(group.indexOf('"') + 1, group.lastIndexOf('"')), baseUrl)
            if (url.startsWith(baseUrl) && !url.endsWith(".xml")) parseNetVersions(url, versions, groupName, readUrls)
        }
    }
}
