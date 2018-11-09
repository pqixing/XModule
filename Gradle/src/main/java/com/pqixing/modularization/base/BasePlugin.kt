package com.pqixing.modularization.base


import com.alibaba.fastjson.JSON
import com.pqixing.Tools
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.ProjectInfo
import com.pqixing.modularization.manager.GitCredential
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.CheckUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.awt.SystemColor.info
import java.io.File
import java.util.*

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin : Plugin<Project>, IPlugin {
    lateinit var p: Project

    private val tasks = HashMap<String, Task>()


    override var projectInfo: ProjectInfo? = null
        get() {
            if (field == null) {
                var infoStr = jsonFromEnv
                if (CheckUtils.isEmpty(infoStr)) {
                    try {
                        val parseClass = GroovyClassLoader().parseClass(File(rootDir, FileNames.PROJECT_INFO))
                        infoStr = JSON.toJSONString(parseClass.newInstance())
                    } catch (e: Exception) {

                    }

                }
                if (!CheckUtils.isEmpty(infoStr)) {
                    try {
                        field = JSON.parseObject(infoStr, ProjectInfo::class.java)
                    } catch (e: Exception) {
                    }

                }
                if (field == null) field = ProjectInfo()
            }
            return field!!
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
        get() = File(buildDir, FileNames.MODULARIZATION)

    override fun apply(project: Project) {
        initProject(project)
    }

    fun setPlugin() {
        pluginCache[javaClass.simpleName] = this
    }

    protected fun initProject(project: Project) {
        this.p = project
        setPlugin()
        initTools(project)
        createIgnoreFile()
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
                , "*.iml")
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
                override fun println(l: String?) = System.out.println(l)
            }, project.rootDir.absolutePath, object : ICredential {
                override fun getUserName() = projectInfo?.gitUserName ?: ""

                override fun getPassWord() = projectInfo?.gitPassWord ?: ""
            })
            FileUtils.init(Keys::class.java)
//            val resource = Keys::class.java.getResource("setting/import.kt")
//            Tools.println("resours-> $resource")
//            val stream = resource.openStream()
//            Tools.println("resours-> ${stream}")
//
//            Tools.println("resours-> ${stream.reader().readText()}")
//            val readText = Keys::class.java.getResourceAsStream("setting/import.kt").reader().readText()
//            Tools.println("resours 2222222-> ${readText}")
        }
    }

    companion object {
        private val pluginCache = HashMap<String, IPlugin>()

        fun <T : IPlugin> getPlugin(pluginClass: Class<T>): T? {
            return pluginCache[pluginClass.name] as T
        }


    }
}
