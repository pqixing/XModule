package com.pqixing.modularization.base


import com.pqixing.modularization.setting.ImportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin : Plugin<Project>, IPlugin {
    private var p: Project? = null


    override val project: Project
        get() = p!!
    override val rootDir: File
        get() = project.rootDir

    override val projectDir: File
        get() = project.projectDir

    override val buildDir: File
        get() = project.buildDir

    override val cacheDir: File
        get() = File(projectDir, "build/${buildDir.name}")


    override fun apply(project: Project) {
        this.p = project
        ImportPlugin.addPlugin(project, this)
    }

    override fun getGradle(): Gradle = project.gradle

    override fun <T> getExtends(tClass: Class<T>): T {
        return project.extensions.getByType(tClass)
    }

    override fun getTask(taskClass: Class<out Task>): Set<Task> {
        return project.getTasksByName(BaseTask.getTaskName(taskClass), false)
    }

}
