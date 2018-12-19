package com.pqixing.modularization.android.tasks

import com.pqixing.Tools
import com.pqixing.git.Components
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.android.dps.DpComponents
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.tools.TextUtils
import java.io.File
import java.util.*
import kotlin.collections.HashSet

/**
 * 依赖对比分析
 * 1,生成模块的依赖分析文件
 * 2，
 */
open class DpsAnalysisTask : BaseTask() {
    init {
        val dpPrint = project.tasks.create("DependencyReport", org.gradle.api.tasks.diagnostics.DependencyReportTask::class.java)
//        dpPrint.outputFile =
        this.dependsOn(dpPrint)
    }


    //依赖分析第一步
    val allDps = LinkedList<Vertex>()
    val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
    var dependentModel: String = ""
        get() {
            return AndroidPlugin.getPluginByProject(project).projectInfo.dependentModel
        }


    override fun runTask() {
        val plugin = AndroidPlugin.getPluginByProject(project)
        val dpsExt = plugin.dpsManager.dpsExt
        //依赖排序起点
        val topVertex = Vertex(project.name)
        allDps.add(topVertex)

        //如果是Api类型，先添加api模块依赖
        if (plugin.APP_TYPE == Components.TYPE_LIBRARY_API) {
            topVertex.dps.add(TextUtils.getApiModuleName(project.name))
        }

        val app = plugin.APP_TYPE == Components.TYPE_APPLICATION

        with(mutableListOf<DpComponents>()) {
            addAll(dpsExt.compiles)
            addAll(dpsExt.apiCompiles)
            forEach {
                //如果当前工程是App类型，添加直接工程依赖
                if (app) topVertex.dps.add(it.moduleName)

                //如果该依赖模块是Api类型，则，依赖添加对应的Api类型依赖，而不是直接依赖该工程本身
                val name = TextUtils.getModuleName(it.moduleName, ProjectManager.findComponent(it.moduleName)?.type == Components.TYPE_LIBRARY_API)
                topVertex.dps.add(name)
            }
        }

        val component = ProjectManager.findComponent(plugin.project.name)!!
        val branch = component.lastLog.branch

        //加载定点依赖的全部依赖
        topVertex.dps.forEach { loadDps(it, branch, dpsExt) }
        allDps.forEach { Tools.println("DpsAnalysisTask -> $it") }

        topoSort(allDps.first)

        val cacheDir = plugin.cacheDir
        val outFile = File(cacheDir, Keys.TXT_DPS_ANALYSIS)
    }

    /**
     * 拓扑排序 将依赖管理有序排序
     */
    private fun topoSort(first: Vertex) {
        val queue = LinkedList<Vertex>()
        queue.offer(first)
        val circles = HashSet<String>()
        Tools.println("topoSort -> $first")
        outer@ while (queue.isNotEmpty()) {
            //后进后出，深度优先查询
            val top = queue.last
            for (d in top.dps) {
                val v = allDps.find { f -> f.name == d } ?: continue
                //如果该依赖不在需要处理的队列时，把依赖加入待处理队列
                if (queue.contains(v).apply { if (this) circles.add("${top.name}<->${v.name}") }
                        || v.degree > top.degree) continue
                //如果该依赖层级增加，则，该依赖的所有依赖层级都需要+1
                v.degree = top.degree + 1
                queue.addLast(v)
                continue@outer//直接轮循下一个
            }
            //循环完毕，移除
            queue.removeLast()
        }
        if (circles.isNotEmpty()) {
            Tools.println("topoSort has circle dependen -> $circles")
        }
        val sort = allDps.asSequence().sortedBy { -it.degree }.map { it.name to it.degree }.toList()
        Tools.println("topoSort list -> $sort")
    }

    fun loadDps(module: String, branch: String, dpsExt: DpsExtends) {

        //如果已经处理过该模块的依赖，不重复处理
        if (allDps.any { it.name == module }) return

        val tempContainer = Vertex(module).apply { allDps.add(this) }.dps
        val compile = when (dependentModel) {
            "localOnly" -> loadDpsFromLocal(module, dpsExt, tempContainer)
            "localFirst" -> loadDpsFromLocal(module, dpsExt, tempContainer) || loadDpsFromMaven(module, branch, tempContainer)
            "mavenFirst" -> loadDpsFromMaven(module, branch, tempContainer) || loadDpsFromLocal(module, dpsExt, tempContainer)
            else -> loadDpsFromMaven(module, branch, tempContainer)
        }

        //依赖解析失败，报错
        if (!compile) Tools.printError("DpsAnalysisTask Exception-> can not resolve dps for $module , mode :$dependentModel")
        tempContainer.forEach { loadDps(it, branch, dpsExt) }
    }

    /**
     * 从本地获取依赖
     */
    fun loadDpsFromLocal(module: String, dpsExt: DpsExtends, dpsContainer: HashSet<String>): Boolean {

        val api = TextUtils.checkIfApiModule(module)
        val mcp = ProjectManager.findComponent(TextUtils.getModuleFromApi(module)) ?: return false

        val path = if (mcp.name == mcp.rootName) mcp.rootName else "${mcp.rootName}/${mcp.name}"

        //查出buildGradle
        val buildGradle = File(FileManager.codeRootDir, "$path/build.gradle")
        val libraryGradle = File(FileManager.docRoot, "gradles/com.module.library.gradle")
        //文件不存，则解析失败
        if (!buildGradle.exists()) return false

        //先清楚旧的依赖数据，然后重新加载
        dpsExt.compiles.clear()
        dpsExt.apiCompiles.clear()

        extHelper.setExtValue(project, "ModuleName", mcp.name)
        try {
            project.apply(mapOf<String, String>("from" to buildGradle.absolutePath))
        } catch (e: Exception) {
        }
        try {
            project.apply(mapOf<String, String>("from" to libraryGradle.absolutePath))
        } catch (e: Exception) {
        }

        (if (api) dpsExt.apiCompiles else dpsExt.compiles).forEach {
            val t = ProjectManager.findComponent(it.moduleName) ?: return@forEach
            //如果是Api类型，直接依赖模块对应的api，而不是该模块本身
            dpsContainer.add(TextUtils.getModuleName(it.moduleName, t.type == Components.TYPE_LIBRARY_API))
        }
        //解析build.gradle文件，加载本地配置的依赖数据
        Tools.println("loadDpsFromLocal $module dps -> $dpsContainer")
        return true
    }

    /**
     * 从本地获取依赖
     */
    fun loadDpsFromMaven(module: String, branch: String, dpsContainer: HashSet<String>): Boolean {
        val version = VersionManager.getVersion(branch, module, "+")
        if (version.first.isEmpty()) return false
        DpsManager.getPom(version.first, module, version.second).dependency.map {
            val p = XmlHelper.strToPair(it)
            if (p.second != null) dpsContainer.add(p.second!!)
        }
        //解析build.gradle文件，加载本地配置的依赖数据
        Tools.println("loadDpsFromMaven $module dps -> $dpsContainer")
        return true
    }

}

/**
 * 依赖处理的临时类，用于进行依赖排序
 */
data class Vertex(var name: String, var degree: Int = 0, var dps: HashSet<String> = HashSet())