package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.help.MavenPom
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.manager.*
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.util.*

class DpsManager(val plugin: AndroidPlugin, val dpsExt: DpsExtends) {

    companion object {
        /**
         * 获取仓库aar中，exclude的传递
         */
        fun getPom(project: Project, branch: String, module: String, version: String): MavenPom {
            val plugin = project.rootPlugin()
            val extends = project.getArgs()
            val pomCache = extends.env.pomCache

            val groupMaven = extends.projectXml.mavenUrl
            val group = "${extends.projectXml.group}.$branch"
            val pomUrl = "$groupMaven/${group.replace(".", "/")}/$module/$version/$module-$version.pom"
            if (!pomUrl.startsWith("http")) {//增加对本地Maven地址的支持
                return XmlHelper.parsePomEclude(FileUtils.readText(File(pomUrl))
                        ?: "", "${extends.projectXml.group}.")
            }

            val pomKey = TextUtils.numOrLetter(pomUrl)
            var pom = pomCache.find { it.first == pomKey }?.apply { pomCache.remove(this);pomCache.addFirst(this) }?.second
            if (pom != null) return pom

            val pomDir = File(plugin.getGradle().gradleHomeDir, "pomCache")
            val pomFile = File(pomDir, pomKey)
            pom = if (pomFile.exists()) XmlHelper.parsePomEclude(FileUtils.readText(pomFile)!!, "${extends.projectXml.group}.")
            else {
                val ponTxt = URL(pomUrl).readText()
                FileUtils.writeText(pomFile, ponTxt)
                XmlHelper.parsePomEclude(ponTxt, extends.projectXml.group)
            }
            pomCache.addFirst(Pair(pomKey, pom))
            //最多保留30条记录
            if (pomCache.size > 30) pomCache.removeLast()
            return pom
        }
    }

    //组件工程
    var project: Project = plugin.project
    var args: ArgsExtends = project.getArgs()
    var compileModel: String = args.config.dependentModel ?: "mavenOnly"
    val loseList = mutableListOf<String>()

    //处理依赖
    fun resolveDps(): String {
        val excludes: HashSet<String> = HashSet()
        val includes: ArrayList<String> = ArrayList()

        val apiModel = plugin.module.apiModule
        if (apiModel != null) {
            dpsExt.compile("${apiModel.name}:${dpsExt.version}*")
        }

        val dps = dpsExt.compiles.toMutableSet()
        if (plugin.buildAsApp) dpsExt.devCompiles.forEach {
            dps.add(it.apply { dpType = "dev" })
        }

        if (dps.isNotEmpty()) {
            val dpsV = mutableListOf<String>()
            dps.forEach { dpc ->
                val compile = when (compileModel) {
                    "localOnly" -> onLocalCompile(dpc, includes, excludes)
                    "localFirst" -> onLocalCompile(dpc, includes, excludes) || onMavenCompile(dpc, includes, excludes)
                    "mavenFirst" -> onMavenCompile(dpc, includes, excludes) || onLocalCompile(dpc, includes, excludes)
                    else -> onMavenCompile(dpc, includes, excludes)
                }
                if (!compile) loseList.add(dpc.moduleName)

                val newVersion = project.getArgs().versions.getVersion(dpc.branch, dpc.moduleName, "+")
                if (!newVersion.second.startsWith(dpc.version) && newVersion.second.trim() != "+" && dpc.version.trim() != "+")
                    dpsV.add("${dpc.version} -> ${newVersion.second} -> compile \"${dpc.moduleName}:${newVersion.second.substringBeforeLast(".")}\"")
            }
            if (dpsV.isNotEmpty())
                Tools.println("----------------------Has New Version----------------------\n ${dpsV.joinToString("\n")}")
        }

        /**
         * 缺失了部分依赖
         */
        if (loseList.isNotEmpty()) {
            if (checkLoseEnable()) Tools.println("ResolveDps -> lose dps -> $loseList")
            else Tools.printError(-1, "ResolveDps -> lose dps -> $loseList")
        }


        val sb = java.lang.StringBuilder("dependencies {  // isApp : ${plugin.isApp} -> buildAsApp : ${plugin.buildAsApp}\n")
        includes.forEach { sb.append(it).append("\n") }
        sb.append("}\n")
                .append("configurations { \n")
                .append(excludeStr("all*.exclude ", excludes.map { XmlHelper.strToPair(it) }.toSet()))
                .append("}\n")
        return sb.toString()
    }

    private fun checkLoseEnable(): Boolean {
        if (args.config.allowLose) return true
        val apiChild = plugin.module.apiModule ?: return false
        return args.runTaskNames.find { it.contains("${apiChild.name}:ToMaven") } != null
    }

    /**
     * 重新凭借scope
     */
    private fun getScope(prefix: String, scope: String): String {
        if (prefix.isEmpty()) return "    $scope"
        return "    $prefix${TextUtils.firstUp(scope)}"
    }

    /**
     * 添加一个仓库依赖
     */
    private fun onMavenCompile(dpc: DpsModel, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        var dpVersion = args.versions.getVersion(dpc.branch, dpc.moduleName, dpc.version)
        if (dpVersion.first.isEmpty() && dpc.matchAuto) dpVersion = args.versions.getVersion(dpc.branch, dpc.moduleName, "+")
        if (dpVersion.first.isEmpty()) {
            return false
        }
        var c = ""
        if (dpVersion.second == dpc.version) {
            c = " ;force = true ;"
        }
        includes.add("${getScope(dpc.dpType, dpc.scope)} ('${args.projectXml.group}.${dpVersion.first}:${dpc.moduleName}:${dpVersion.second}') { ${excludeStr(excludes = dpc.excludes)} $c }")
        addBranchExclude(dpVersion.first, dpc.moduleName, excludes)
        excludes.addAll(getPom(project, dpVersion.first, dpc.moduleName, dpVersion.second).allExclude)
        return true
    }

    /**
     * 检查是否存在其他分支的版本，如果存在，添加到exclude中
     */
    private fun addBranchExclude(compileBranch: String, moduleName: String, excludes: HashSet<String>, pointer: Int = 1) {
        with(args.projectXml.matchingFallbacks) {
            val start = indexOf(compileBranch) + pointer
            for (i in start until size) {
                val b = if (i < 0) compileBranch else get(i)
                if (args.versions.checkBranchVersion(b, moduleName)) {
                    excludes.add("${args.projectXml.group}.$b,$moduleName")
                }
            }
        }
    }

    /**
     * 生成exlude字符串
     */
    private fun excludeStr(prefix: String = "exclude", excludes: Set<Pair<String?, String?>>): String {
        val sb = StringBuilder()
        excludes.forEach {
            sb.append("    $prefix (")
            if (it.first != null) sb.append("group : '${it.first}',")
            if (it.second != null) sb.append("module : '${it.second}',")
            sb.deleteCharAt(sb.length - 1)
            sb.append(") \n")
        }
        return sb.toString()
    }

    /**
     * 本地进行工程依赖
     */
    private fun onLocalCompile(dpc: DpsModel, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        val branch = dpc.module.getBranch()
        if (branch != plugin.module.getBranch() && !args.config.allowDpDiff) {
            Tools.println("    branch diff ${dpc.moduleName} -> $branch")
            return false
        }
        //如果该依赖没有本地导入，不进行本地依赖
        dpc.localCompile = true
        includes.add("${getScope(dpc.dpType, dpc.scope)} ( project(path : ':${dpc.moduleName}'))  { ${excludeStr(excludes = dpc.excludes)} }")
        addBranchExclude(branch, dpc.moduleName, excludes, 0)
        return true
    }


}