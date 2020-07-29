package com.pqixing.modularization.maven

import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.getEnvValue
import com.pqixing.help.MavenPom
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.root.getArgs
import com.pqixing.modularization.root.rootPlugin
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import com.pqixing.tools.TextUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class VersionManager(val args: ArgsExtends) {
    val matchingFallbacks get() = args.manifest.fallbacks.toMutableList()
    val groupName get() = args.manifest.groupId

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()

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
        val branchFile = File(args.env.basicDir, getBranchVersionName(branch))
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
        targetVersion[FileNames.XMODULE] = FileNames.XMODULE
        val info = args.config
        PropertiesUtils.readProperties(File(info.versionFile)).forEach {
            targetVersion[it.key.toString()] = it.value.toString()
        }
    }

    private fun readCurVersions() {
        if (curVersions.isNotEmpty()) return
        val versionFile = args.env.versionFile

        val basicRepoUrl = TextUtils.append(arrayOf(args.manifest.mavenUrl, groupName.replace(".", "/"), EnvKeys.BASIC))
        PropertiesUtils.readProperties(versionFile).forEach { curVersions[it.key.toString()] = it.value.toString() }

        //如果是打包，则从仓库更新版本
        if (args.config.sync || !versionFile.exists() || args.runTaskNames.toString().contains("ToMaven")) {//如果当前文件不存，从新生成
            val metaTxt = readUrlTxt(TextUtils.append(arrayOf(basicRepoUrl, EnvKeys.XML_META)))
            val cacheDir = File(args.env.versionDir, "cache")
            val meta = XmlHelper.parseMetadata(metaTxt)

            val oldUpdate = curVersions["lastUpdated"]?.toLongOrNull() ?: 0
            val newUpdate = meta.lastUpdated.toLongOrNull() ?: 0
            //如果仓库的最后更新时间，比当前的最后更新时间新，则从新加载文件
            if (newUpdate > oldUpdate) {
                val newVersion = mutableMapOf<String, Int>()
                meta.versions.reversed().filterIndexed { index, _ -> index < 3 }.forEach { v ->
                    val cacheFile = File(cacheDir, v)
                    var pom = FileUtils.readText(cacheFile)
                    if (pom?.isNotEmpty() != true) {
                        pom = readUrlTxt(TextUtils.append(arrayOf(basicRepoUrl, v, "${EnvKeys.BASIC}-${v}.properties")))
                        FileUtils.writeText(cacheFile, pom)
                    }

                    for (entry in PropertiesUtils.readProperties(cacheFile).toMap()) {
                        val value = entry.value.toString().toIntOrNull() ?: continue

                        val key = entry.key.toString()
                        newVersion[key] = value.coerceAtLeast(newVersion[key] ?: 0)
                    }
                }
                newVersion.forEach { curVersions[it.key] = it.value.toString() }
                curVersions["lastUpdated"] = newUpdate.toString()
                PropertiesUtils.writeProperties(versionFile, newVersion.map { it.key to it.value.toString() }.toMap().toProperties())
            }
        }
        if (curVersions.isEmpty()) curVersions["lastUpdated"] = "0"
    }

    /**
     * 把每个版本的最后版本号添加
     */
    private fun addVersion(curVersions: HashMap<String, String>, groupId: String, artifactId: String, version: List<String>) {
//        Tools.println("addVersion -> $groupId $artifactId $version")
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
        indexVersionFromNet(File(args.env.basicDir, "versions/version.properties"), curVersions)
    }

    /**
     * 创建一个分支的版本号Tag标签
     */
    fun createVersionTag(): Boolean {
        val opBranch = EnvKeys.opBranch.getEnvValue() ?: return false
        val taskBranch = opBranch.substring(opBranch.lastIndexOf("/") + 1)//直接获取名称,不要origin
        if (taskBranch.isEmpty() || taskBranch == "master") {
            Tools.printError(-1, "createVersionTag taskBranch is empty, please input taskBranch!!")
            return false
        }
        //拷贝一份
        val fallbacks = matchingFallbacks.toMutableList()
        (EnvKeys.tagBranch.getEnvValue()
                ?: "").split(",").map { it.split("/").last().trim() }.forEach { if (it.isNotEmpty() && !fallbacks.contains(it)) fallbacks.add(it) }

        val matchKeys = fallbacks.map { "$groupName.$it." }
        val tagVersions = curVersions.filter { c -> matchKeys.any { f -> c.key.startsWith(f) } }

        val branchFile = File(args.env.basicDir, getBranchVersionName(taskBranch))
        PropertiesUtils.writeProperties(branchFile, tagVersions.toProperties())
        ResultUtils.writeResult(branchFile.absolutePath)
//        val git = Git.open(args.env.basicDir)
//        GitUtils.addAndPush(git, "versions", "createVersionTag $taskBranch ${DateFormat.getDateTimeInstance().format(Date())}", true)
//        GitUtils.close(git)
        return true
    }

    /**
     * 从网络获取最新的版本号信息
     */
    private fun indexVersionFromNet(outFile: File, versions: HashMap<String, String>) {

        val extends = args
        val maven = extends.manifest.mavenUrl
        val groupUrl = extends.manifest.groupId.replace(".", "/")
        Tools.println("parseNetVersions  start -> $groupUrl")
        val start = System.currentTimeMillis()
        versions.clear()
        //上传版本好到服务端
        val git = Git.open(args.env.basicDir)

        if (!maven.startsWith("http")) {
            parseLocalVersion(File(maven, groupUrl), versions)
        } else parseNetVersionsForTarget(maven, groupName, git, versions)

        Tools.println("parseNetVersions  end -> ${System.currentTimeMillis() - start} ms")
        versions[Keys.UPDATE_TIME] = (System.currentTimeMillis() / 1000).toInt().toString()

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
    fun readUrlTxt(url: String) = kotlin.runCatching {
        if (url.startsWith("http")) URL(url).readText() else FileUtils.readText(File(url))
    }.getOrNull() ?: ""

    fun getFullUrl(url: String, baseUrl: String): String {
        if (url == "..") return "";
        if (url.startsWith("http:")) return url
        if (baseUrl.endsWith("/")) return "$baseUrl$url"
        return "$baseUrl/$url"
    }

    fun parseNetVersionsForTarget(baseUrl: String, groupName: String, git: Git, versions: HashMap<String, String>) {
        val allModules = args.manifest.allModules().map { it.name }
        GitUtils.pull(git)
        val mavenUrl = getFullUrl(groupName.replace(".", "/"), baseUrl)
        val allBranchs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().map { it.name.replace(Regex(".*/"), "") }.toSet()

        allBranchs.forEach { b ->
            allModules.forEach { m ->
                val url = getFullUrl("${b.replace(".", "/")}/$m/maven-metadata.xml", mavenUrl)

                val metaStr = readUrlTxt(url)
                if (metaStr.isNotEmpty()) kotlin.runCatching {
                    val meta = XmlHelper.parseMetadata(metaStr)
                    Tools.println("request -> $url -> ${meta.versions}")
                    addVersion(versions, meta.groupId.trim(), meta.artifactId.trim(), meta.versions)
                }
            }
        }
    }

    /**
     * 解析maven仓库，爬取当前group的所有版本
     */
    fun parseNetVersions(baseUrl: String, versions: HashMap<String, String>, groupName: String, readUrls: HashSet<String> = HashSet()) {
        if (readUrls.contains(baseUrl)) return//防止重复请求处理
        readUrls.add(baseUrl)
        val htmlText = readUrlTxt(baseUrl)
        var matcher = Pattern.compile("<a href=.*?>maven-metadata.xml</a>").matcher(htmlText)
        if (matcher.find()) {
            val group = matcher.group()
            val meteUrl = group.substring(group.indexOf('"') + 1, group.lastIndexOf('"'))
            val meta = XmlHelper.parseMetadata(readUrlTxt(getFullUrl(meteUrl, baseUrl)))
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

    fun storeToUp(curVersions: MutableMap<String, String>) {
        curVersions[Keys.UPDATE_TIME] = (System.currentTimeMillis() / 1000).toInt().toString()
        PropertiesUtils.writeProperties(args.env.defArchivesFile, curVersions.toProperties())//保存当前的版本信息等待上传
    }

    /**
     * 获取仓库aar中，exclude的传递
     */
    fun getPom(project: Project, branch: String, module: String, version: String): MavenPom {
        val plugin = project.rootPlugin()
        val extends = project.getArgs()

        val groupMaven = extends.manifest.mavenUrl
        val group = "${extends.manifest.groupId}.$branch"
        val pomUrl = "$groupMaven/${group.replace(".", "/")}/$module/$version/$module-$version.pom"
        if (!pomUrl.startsWith("http")) {//增加对本地Maven地址的支持
            return XmlHelper.parsePomExclude(FileUtils.readText(File(pomUrl))
                    ?: "", "${extends.manifest.groupId}.")
        }

        val pomKey = TextUtils.numOrLetter(pomUrl)
        var pom = pomCache.find { it.first == pomKey }?.apply { pomCache.remove(this);pomCache.addFirst(this) }?.second
        if (pom != null) return pom

        val pomDir = File(plugin.getGradle().gradleHomeDir, "pomCache")
        val pomFile = File(pomDir, pomKey)
        pom = if (pomFile.exists()) XmlHelper.parsePomExclude(FileUtils.readText(pomFile)!!, "${extends.manifest.groupId}.")
        else {
            val ponTxt = URL(pomUrl).readText()
            FileUtils.writeText(pomFile, ponTxt)
            XmlHelper.parsePomExclude(ponTxt, extends.manifest.groupId)
        }
        pomCache.addFirst(Pair(pomKey, pom))
        //最多保留30条记录
        if (pomCache.size > 30) pomCache.removeLast()
        return pom
    }
}
