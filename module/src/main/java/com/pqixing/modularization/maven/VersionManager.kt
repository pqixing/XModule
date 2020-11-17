package com.pqixing.modularization.maven

import com.pqixing.EnvKeys
import com.pqixing.help.MavenPom
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.modularization.setting.ImportPlugin.Companion.getArgs
import com.pqixing.modularization.setting.ImportPlugin.Companion.rootXPlugin
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import com.pqixing.tools.TextUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class VersionManager(val args: ArgsExtends) {
    //分支tag的的路径名字
    val tagVersions = mutableMapOf<String, String>()
    var lastVersion = "default"
    val loads = mutableSetOf<String>()

    val fallbacks get() = args.manifest.fallbacks.toMutableList()
    val groupName get() = args.manifest.groupId

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()

    /**
     * 当前最新的版本信息
     */
    private val curVersions = HashMap<String, Int>()

    /**
     * 分支相关版本号信息
     */
    private val branchVersion = HashMap<String, HashMap<String, Int>>()

    /**
     * 强制指定的版本号信息，分支默认会使用指定的分支信息
     */
    private val targetVersion = HashMap<String, Int>()

    /**
     * 获取指定分支指定baseVersion版本的最新版本号
     */
    fun getNewerVersion(branch: String, module: String, version: String): Int = readCurVersions()["$groupName.$branch.$module.$version"]
            ?: -1

    /**
     * 根据分支，查找出所有模块名称
     */
    fun findAllModuleByBranch(branch: String): Set<String> {
        val preKey = "$groupName.$branch."
        return readCurVersions().keys.filter { it.startsWith(preKey) }.map {
            val r = it.replace(preKey, "")
            r.substring(0, r.indexOf("."))
        }.toSet()
    }


    /**
     * 按照顺序，查取模块的版本号信息
     * 指定版本 > 分支版本 > 当前版本
     */
    fun getVersion(branch: String, module: String, inputVersion: String): Pair<String, String> {
        val branchVersion = readBranchVersion(branch)
        val start = fallbacks.indexOf(branch)
        for (i in start until fallbacks.size) {
            val b = if (i < 0) branch else fallbacks[i]
            val preKey = "$groupName.$b.$module."
            var version = inputVersion
            if (TextUtils.isVersionCode(version)) {
                val i1 = version.lastIndexOf('.')
                if (i1 < 0) continue
                val baseVersion = version.substring(0, i1)
                val last = version.substring(i1 + 1).toInt()
                val v = branchVersion["$preKey$baseVersion"] ?: continue
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
        val branchVersion = readBranchVersion(branch)
        val preKey = "$groupName.$branch.$module."
        return TextUtils.isBaseVersion(findBaseVersion("+", preKey, branchVersion))
    }

    private fun findBaseVersion(v: String, preKey: String, versions: HashMap<String, Int>): String {
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

    @Synchronized
    fun readBranchVersion(branch: String): HashMap<String, Int> {
        val hash = DigestUtils.md5Hex(branch);
        if (loads.contains(hash)) return branchVersion[branch]!!

        val branchMap = HashMap<String, Int>(readCurVersions())

        //打过标签的文件
        branchMap += readVersionFromFile(EnvKeys.BASIC_TAG, tagVersions[hash])
        branchVersion[branch] = branchMap

        branchMap += readTargetVersions()
        return branchMap
    }

    /**
     * 读取指定的版本文件，优先级最高
     */
    @Synchronized
    private fun readTargetVersions(): HashMap<String, Int> {
        if (!loads.contains("targetVersion")) {
            loads.add("targetVersion")
            val info = args.config
            PropertiesUtils.readProperties(File(info.versionFile)).forEach {
                targetVersion[it.key.toString()] = it.value.toString().toIntOrNull() ?: 0
            }
        }
        return targetVersion
    }

    fun readVersionFromFile(artifactId: String, version: String?): Map<String, Int> {
        version ?: return emptyMap()
        val versionDir = XmlHelper.fileVersion(args.env.rootDir.absolutePath)
        val file = File(versionDir, "$artifactId/${version}.txt")
        if (!file.exists()) {
            val netTxt = XmlHelper.readUrlTxt(args.manifest.fullUrl(artifactId, version, "${artifactId}-${version}.txt"))
            if (netTxt.isNotEmpty()) {//写入网络的文件
                FileUtils.writeText(file, netTxt)
            }
        }
        val map = mutableMapOf<String, Int>()
        //加载当前版本号
        for (it in PropertiesUtils.readProperties(file)) {
            map[it.key.toString()] = it.value.toString().toIntOrNull() ?: 0
        }
        return map
    }

    @Synchronized
    fun readCurVersions(): HashMap<String, Int> {
//        println("readCurVersions---- -> $curVersions")
        if (loads.contains("curVersions")) return curVersions
        val basePath = args.env.rootDir.absolutePath
        val versionDir = XmlHelper.fileVersion(basePath)
        //重新冲仓库更新一次版本信息
        if (args.config.sync || !versionDir.exists() || args.runTaskNames.find { it.contains("ToMaven") } != null) {//如果当前文件不存，从新生成
            XmlHelper.loadVersionFromNet(basePath)
        }

        lastVersion = XmlHelper.parseMetadata(FileUtils.readText(File(versionDir, "${EnvKeys.BASIC}.xml"))).versions.lastOrNull()
                ?: "default"

        //加载所有版本信息相关的文件
        for (v in XmlHelper.parseMetadata(FileUtils.readText(File(versionDir, "${EnvKeys.BASIC_TAG}.xml"))).versions) {
            tagVersions[v.substring(0, v.lastIndexOf("-"))] = v
        }

        //加载full版本记录
        curVersions += readVersionFromFile(EnvKeys.BASIC, lastVersion.takeIf { it != "default" })

        //根据full版本，加载临时提交记录进行合并
        val logMeta = XmlHelper.parseMetadata(FileUtils.readText(File(versionDir, "${EnvKeys.BASIC_LOG}/${lastVersion}.xml")))
        for (it in logMeta.versions) {
            val spilt = it.lastIndexOf(".")
            if (spilt < 0) continue
            val key = it.substring(0, spilt)
            val value = it.substring(spilt + 1).toIntOrNull() ?: 0
            //放入最大值
            curVersions[key] = value.coerceAtLeast(curVersions[key] ?: 0)
        }
//        Tools.println("parse---$lastVersion -> ${logMeta.versions} -> $curVersions")

        loads.add("curVersions")
        return curVersions
    }

    fun storeToUp(map: Map<String, Any?> = emptyMap()) {
        Tools.println("storeToUp ->")
        PropertiesUtils.writeProperties(args.env.archivesFile, map.map { it.key to it.value.toString() }.toMap().toProperties())//保存当前的版本信息等待上传
    }

    /**
     * 获取仓库aar中，exclude的传递
     */
    fun getPom(project: Project, branch: String, module: String, version: String): MavenPom {
        val plugin = project.rootXPlugin()
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
