package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.help.MavenPom
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.IExtHelper
import com.pqixing.modularization.manager.ManagerExtends
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.io.File
import java.net.URL
import java.util.*

class DpsManager(val plugin: AndroidPlugin, val dpsExt: DpsExtends) : OnClear {
    init {
        BasePlugin.addClearLister(this)
        start()
    }

    override fun clear() {
        pomCache.clear()
        loseList.clear()
    }

    companion object {
        //内存中只保留10跳
        var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()

        /**
         * 获取仓库aar中，exclude的传递
         */
        fun getPom(branch: String, module: String, version: String): MavenPom {
            val plugin = ManagerPlugin.getPlugin()
            val extends = plugin.getExtends(ManagerExtends::class.java)
            val groupMaven = extends.groupMaven
            val group = "${extends.groupName}.$branch"
            val pomUrl = "$groupMaven/${group.replace(".", "/")}/$module/$version/$module-$version.pom"
            if (!pomUrl.startsWith("http")) {//增加对本地Maven地址的支持
                return XmlHelper.parsePomEclude(FileUtils.readText(File(pomUrl)) ?: "", "${extends.groupName}.")
            }

            val pomKey = TextUtils.numOrLetter(pomUrl)
            var pom = pomCache.find { it.first == pomKey }?.apply { pomCache.remove(this);pomCache.addFirst(this) }?.second
            if (pom != null) return pom

            val pomDir = File(plugin.getGradle().gradleHomeDir, "pomCache")
            val pomFile = File(pomDir, pomKey)
            pom = if (pomFile.exists()) XmlHelper.parsePomEclude(FileUtils.readText(pomFile)!!, "${extends.groupName}.")
            else {
                val ponTxt = URL(pomUrl).readText()
                FileUtils.writeText(pomFile, ponTxt)
                XmlHelper.parsePomEclude(ponTxt, extends.groupName)
            }
            pomCache.addFirst(Pair(pomKey, pom))
            //最多保留30条记录
            if (pomCache.size > 30) pomCache.removeLast()
            return pom
        }
    }

    //组件工程
    lateinit var compileModel: String
    lateinit var managerExtends: ManagerExtends

    val loseList = mutableListOf<String>()
    override fun start() {
        compileModel = plugin.config.dependentModel ?: "mavenOnly"
        managerExtends = ManagerPlugin.getExtends()
    }

    //处理依赖
    fun resolveDps(): String {
        val excludes: HashSet<String> = HashSet()
        val includes: ArrayList<String> = ArrayList()

        onSelfCompile()

        val dps = dpsExt.compiles.toMutableSet()
        if (plugin.justSync || plugin.buildAsApp) dpsExt.devCompiles.forEach {
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
                val newVersion = VersionManager.getVersion(dpc.branch, dpc.moduleName, "+")
                if (!newVersion.second.startsWith(dpc.version) && newVersion.second.trim() != "+" && dpc.version.trim() != "+")
                    dpsV.add("\n       Config Version : ${dpc.version} -> Last Version : ${newVersion.second} -> Match Branch : ${newVersion.first} -> ${dpc.moduleName} ")
                if (dpc.emptyVersion) dpc.version = newVersion.second
            }
            if (dpsV.isNotEmpty())
                Tools.println("${plugin.project.name} -> Dependency Diff BaseVersion $dpsV")
        }

        /**
         * 缺失了部分依赖
         */
        if (loseList.isNotEmpty()) {
            if (checkLoseEnable()) Tools.println("ResolveDps -> lose dps -> $loseList")
            else Tools.printError(-1, "ResolveDps -> lose dps -> $loseList")
        }

        val extends = ManagerPlugin.getExtends()
        val allows = extends.emptyVersions
        val emptyVersions = dps.filter { it.emptyVersion && !allows.contains(it.moduleName) }
        if (emptyVersions.isNotEmpty()) {
            Tools.println("------------------------------WARNING------------------------------")
            emptyVersions.forEach {
                Tools.println("compile \"${it.moduleName}:${it.version.substringBeforeLast(".")}\"")
            }
            if (!extends.allowEmptyVerion) Tools.println("------------------------------ERROR:Not Allow Empty Version For The Above Modules------------------------------")
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
        if (plugin.config.allowLose) return true
        val apiChild = plugin.subModule.child ?: return false
        return loseList.size == 1 && loseList[0] == apiChild.name && plugin.runTaskNames.find { it.contains("${apiChild.name}:ToMaven") } != null
    }


    /**
     * 处理自身的依赖，主要针对Library_api类型
     */
    private fun onSelfCompile() {
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        extHelper.addSourceDir(plugin.project, File(plugin.cacheDir, "java").absolutePath)

        //如果当前模块，有api模块，则，默认添加对api模块的依赖
        val apiModule = plugin.subModule.findApi() ?: return
        dpsExt.compile("${apiModule.name}:${dpsExt.toMavenVersion}")
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
    private fun onMavenCompile(dpc: DpComponents, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        val dpVersion = VersionManager.getVersion(dpc.branch, dpc.moduleName, dpc.version)
        if (dpVersion.first.isEmpty()) return false
        var c = ""
        if (dpVersion.second == dpc.version) {
            c = " ;force = true ;"
        }
        includes.add("${getScope(dpc.dpType, dpc.scope)} ('${managerExtends.groupName}.${dpVersion.first}:${dpc.moduleName}:${dpVersion.second}') { ${excludeStr(excludes = dpc.excludes)} $c }")
        addBranchExclude(dpVersion.first, dpc.moduleName, excludes)
        excludes.addAll(getPom(dpVersion.first, dpc.moduleName, dpVersion.second).allExclude)
        return true
    }

    /**
     * 检查是否存在其他分支的版本，如果存在，添加到exclude中
     */
    private fun addBranchExclude(compileBranch: String, moduleName: String, excludes: HashSet<String>, pointer: Int = 1) {
        with(managerExtends.matchingFallbacks) {
            val start = indexOf(compileBranch) + pointer
            for (i in start until size) {
                val b = if (i < 0) compileBranch else get(i)
                if (VersionManager.checkBranchVersion(b, moduleName)) {
                    excludes.add(XmlHelper.pairToStr("${managerExtends.groupName}.$b", moduleName))
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
    private fun onLocalCompile(dpc: DpComponents, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        val localProject = plugin.project.rootProject.allprojects.find { it.name == dpc.moduleName }
                ?: return false
        if (!dpc.subModule.hasCheck) ProjectManager.checkProject(localProject)
        val branch = dpc.subModule.getBranch()
        if (branch != plugin.subModule.getBranch() && !plugin.config.allowDpDiff) {
            Tools.println("onLocalCompile can not dependent other branch project!!!${dpc.moduleName} $branch")
            return false
        }
        //如果该依赖没有本地导入，不进行本地依赖
        dpc.localCompile = true
        includes.add("${getScope(dpc.dpType, dpc.scope)} ( project(path : ':${dpc.moduleName}'))  { ${excludeStr(excludes = dpc.excludes)} }")
        addBranchExclude(branch, dpc.moduleName, excludes, 0)
        return true
    }


}