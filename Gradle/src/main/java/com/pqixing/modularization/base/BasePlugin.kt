package com.pqixing.modularization.base


import com.alibaba.fastjson.JSON
import com.pqixing.ProjectInfo
import com.pqixing.Tools
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import java.io.File
import java.util.*

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin : Plugin<Project>, IPlugin {
    lateinit var p: Project
    val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)

    protected abstract val applyFiles: List<String>

    private val tasks = HashMap<String, Task>()

    override var projectInfo: ProjectInfo = ProjectInfo()
        get() {
            if (pi == null) {

                pi = try {
                    val parseClass = GroovyClassLoader().parseClass(File(rootDir, FileNames.PROJECT_INFO))
                    JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), ProjectInfo::class.java)
                } catch (e: Exception) {
                    ProjectInfo()
                }
                loadProjectInfo(pi!!)
                Tools.println(JSON.toJSONString(pi))
            }
            return pi!!
        }

    /**
     * 从系统配置中加载对应的变量
     */
    private fun loadProjectInfo(pi: ProjectInfo) {
        pi.javaClass.fields.forEach {
            val value = TextUtils.getSystemEnv(it.name) ?: return@forEach
            try {
                it.isAccessible = true
                when (it.type) {
                    Boolean::class.java -> it.setBoolean(pi, value.toBoolean())
                    String::class.java -> it.set(pi, value)
                }
            } catch (e: Exception) {
                Tools.println("loadProjectInfo Exception -> $e")
            }
        }
    }

    private val jsonFromEnv: String
        get() {
            val infoStr = TextUtils.getSystemEnv("ProjectInfo") ?: return ""
            return String(Base64.getDecoder().decode(infoStr.toByteArray()))
        }

    override val project: Project
        get() = p
    override val rootDir: File
        get() = project.rootDir

    override val projectDir: File
        get() = project.projectDir

    override val buildDir: File
        get() = project.buildDir

    override val cacheDir: File
        get() {
            val suffix = if (project == project.rootProject) "" else "_${buildDir.name}"
            return File(projectDir, "build/${FileNames.MODULARIZATION}$suffix")
        }

    override fun apply(project: Project) {
        initProject(project)
    }

    override fun getGradle(): Gradle = p.gradle


    protected fun initProject(project: Project) {
        this.p = project
        initTools(project)
        createIgnoreFile()

        val file = File(FileManager.docRoot, "gradles")
        extHelper.setExtValue(project, "gradles", file.absolutePath)

        callBeforeApplyMould()
        applyFiles.forEach {
            val f = File(file, "$it.gradle")
            if (f.exists() && f.isFile)
                project.apply(mapOf<String, String>("from" to f.absolutePath))
        }
        linkTask()?.forEach { onTaskCreate(it, BaseTask.task(project, it)) }
    }

    protected fun onTaskCreate(taskClass: Class<*>, task: Task) {

    }

    override fun <T> getExtends(tClass: Class<T>): T {
        return project.extensions.getByType(tClass)
    }

    override fun getTask(taskClass: Class<out Task>): Set<Task> {
        return project.getTasksByName(BaseTask.getTaskName(taskClass), false)
    }

    fun createIgnoreFile() {
        val ignoreFile = project.file(FileNames.GIT_IGNORE)
        val defSets = mutableSetOf<String>("build"
                , Keys.FOCUS_GRADLE
                , FileNames.MODULARIZATION
                , "*.iml", "import.kt")
        defSets += ignoreFields
        val old = FileUtils.readText(ignoreFile) ?: ""
        old.lines().forEach { line -> defSets.remove(line.trim()) }

        if (defSets.isEmpty()) return
        val txt = StringBuilder(old)
        defSets.forEach { txt.append("\n$it") }
        FileUtils.writeText(ignoreFile, txt.toString())
    }

    private fun initTools(project: Project) {
        if (!Tools.init) {
            Tools.init(object : ILog {
                override fun printError(exitCode: Int, l: String?) = ResultUtils.writeResult(l
                        ?: "", exitCode)

                override fun println(l: String?) = System.out.println(l)
            }, project.rootDir.absolutePath, object : ICredential {
                override fun getUserName() = projectInfo?.gitUserName ?: ""

                override fun getPassWord() = projectInfo?.gitPassWord ?: ""
            })
        }
    }

    companion object {
        var pi: ProjectInfo? = null
        val listeners = mutableSetOf<OnClear>()
        fun addClearLister(l: OnClear) {
            listeners.add(l)
        }

        fun onClear() {
            pi = null
            listeners.forEach { it.clear() }
        }
    }
}
