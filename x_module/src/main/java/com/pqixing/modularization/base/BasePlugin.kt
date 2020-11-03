package com.pqixing.modularization.base


import com.pqixing.EnvKeys
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.setting.ImportPlugin
import com.pqixing.tools.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin : Plugin<Project>, IPlugin {
    lateinit var p: Project
    val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)


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
            val suffix = if (project == project.rootProject) "" else buildDir.name
            return File(projectDir, "build/$suffix")
        }

    override fun apply(project: Project) {
        this.p = project
        ImportPlugin.addPlugin(project,this)
        createIgnoreFile()

        linkTask().forEach { onTaskCreate(it, BaseTask.task(project, it)) }
    }

    override fun getGradle(): Gradle = p.gradle

    protected fun onTaskCreate(taskClass: Class<*>, task: Task) {

    }

    override fun <T> getExtends(tClass: Class<T>): T {
        return project.extensions.getByType(tClass)
    }

    override fun getTask(taskClass: Class<out Task>): Set<Task> {
        return project.getTasksByName(BaseTask.getTaskName(taskClass), false)
    }

    fun createIgnoreFile() {
        val ignoreFile = project.file(EnvKeys.GIT_IGNORE)

        val defSets = mutableSetOf("build", "*.iml", "src/dev")
        defSets += ignoreFields
        val old = FileUtils.readText(ignoreFile) ?: ""
        old.lines().forEach { line -> defSets.remove(trimIgnoreKey(line.trim())) }

        if (defSets.isEmpty()) return
        val txt = StringBuilder(old)
        defSets.forEach { txt.append("\n$it") }
        FileUtils.writeText(ignoreFile, txt.toString())
    }

    fun trimIgnoreKey(key: String): String {
        var start = 0
        var end = key.length
        if (key.startsWith("/")) start++
        if (key.endsWith("/")) end--
        return key.substring(start, end)
    }
}
