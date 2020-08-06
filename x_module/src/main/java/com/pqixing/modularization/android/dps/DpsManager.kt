package com.pqixing.modularization.android.dps

import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.Compile
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.root.getArgs
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.tools.TextUtils
import org.gradle.api.Project
import java.util.*

class DpsManager(val plugin: AndroidPlugin) {


    //组件工程
    var project: Project = plugin.project
    var args: ArgsExtends = project.getArgs()
    var compileModel: String = args.config.dependentModel ?: "mavenOnly"
    val loseList = mutableListOf<String>()
    var localProject = setOf<String>()
    val module = plugin.module

    //处理依赖
    fun resolveDps(): String {
        val excludes: HashSet<String> = HashSet()
        val includes: ArrayList<String> = ArrayList()
        localProject = project.rootProject.allprojects.map { it.name }.toSet()

        val dps = module.compiles.toMutableSet()
        if (plugin.buildAsApp) module.devCompiles.forEach { dps.add(it.apply { dpType = "dev" }) }

        if (dps.isNotEmpty()) {
            val dpsV = mutableListOf<String>()
            dps.forEach { dpc ->
                val compile = when (compileModel) {
                    "localOnly" -> onLocalCompile(dpc, includes, excludes)
                    "localFirst" -> onLocalCompile(dpc, includes, excludes) || onMavenCompile(dpc, includes, excludes)
                    "mavenFirst" -> onMavenCompile(dpc, includes, excludes) || onLocalCompile(dpc, includes, excludes)
                    else -> onMavenCompile(dpc, includes, excludes)
                }
                if (!compile) {
                    Tools.println("lose -> ${dpc.name}")
                    loseList.add(dpc.name)
                }

                val newVersion = project.getArgs().vm.getVersion(dpc.branch, dpc.name, "+")
                if (!newVersion.second.startsWith(dpc.version) && newVersion.second.trim() != "+" && dpc.version.trim() != "+")
                    dpsV.add("${dpc.version} -> ${newVersion.second} -> compile \"${dpc.name}:${newVersion.second.substringBeforeLast(".")}\"")
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


        val sb = java.lang.StringBuilder("dependencies { \n")
        includes.forEach { sb.append(it).append("\n") }
        sb.append("}\n")
                .append("configurations { \n")
                .append(excludeStr("all*.exclude ", excludes.map { XmlHelper.strToPair(it) }.toSet()))
                .append("}\n")
        return sb.toString()
    }

    private fun checkLoseEnable(): Boolean {
        if (args.config.allowLose) return true
        val apiChild = plugin.module.api ?: return false
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
    private fun onMavenCompile(dpc: Compile, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        val dpVersion = args.vm.getVersion(dpc.branch, dpc.name, dpc.version).takeIf { it.first.isNotEmpty() }
                ?: return false

        val c = takeIf { dpVersion.second == dpc.version }?.let { " ;force = true ;" } ?: ""

        includes.add("${getScope(dpc.dpType, dpc.scope)} ('${args.manifest.groupId}.${dpVersion.first}:${dpc.name}:${dpVersion.second}') { ${excludeStr(excludes = dpc.excludes)} $c }")
        addBranchExclude(dpVersion.first, dpc.name, excludes)
        excludes.addAll(args.vm.getPom(project, dpVersion.first, dpc.name, dpVersion.second).allExclude)
        return true
    }
//
//    /**
//     * 自动匹配版本号
//     */
//    private fun resolveVersion(dpc: Compile) {
//        if (dpc.version.isNotEmpty()) return
//
//        //没有依附模块,则读取当前配置的版本
//        dpc.version = dpc.attach?.also { resolveVersion(it) }?.let { attach ->
//            kotlin.runCatching {
//                getPom(project, attach.branch, attach.name,
//                        args.versions.getVersion(attach.branch, attach.name, attach.version).second).dependency.find { it.contains(":${dpc.name}:") }
//            }.getOrNull()?.let { it.replace("\"", "").substringAfterLast(":") }
//        } ?: dpc.version.let { "*$it" } ?: "+"
//    }

    /**
     * 检查是否存在其他分支的版本，如果存在，添加到exclude中
     */
    private fun addBranchExclude(compileBranch: String, moduleName: String, excludes: HashSet<String>, pointer: Int = 1) {
        with(args.manifest.fallbacks) {
            val start = indexOf(compileBranch) + pointer
            for (i in start until size) {
                val b = if (i < 0) compileBranch else get(i)
                if (args.vm.checkBranchVersion(b, moduleName)) {
                    excludes.add("${args.manifest.groupId}.$b,$moduleName")
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
    private fun onLocalCompile(dpc: Compile, includes: ArrayList<String>, excludes: HashSet<String>): Boolean {
        if (!localProject.contains(dpc.name) && !args.config.allowDpDiff) return false
        
        val branch = dpc.module.getBranch()
        if (branch != plugin.module.getBranch()) Tools.println("    branch diff ${dpc.name} -> $branch")
        //如果该依赖没有本地导入，不进行本地依赖
        dpc.local = true
        includes.add("${getScope(dpc.dpType, dpc.scope)} ( project(path : ':${dpc.name}'))  { ${excludeStr(excludes = dpc.excludes)} }")
        addBranchExclude(branch, dpc.name, excludes, 0)
        return true
    }


}