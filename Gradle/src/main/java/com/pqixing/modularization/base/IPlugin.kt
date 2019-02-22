package com.pqixing.modularization.base

import com.pqixing.ProjectInfo

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle

import java.io.File

interface IPlugin {
    val ignoreFields: Set<String>
    val cacheDir: File
    val buildDir: File
    val projectDir: File
    val rootDir: File

    val project: Project
    val config: ProjectInfo
    fun getTask(taskClass: Class<out Task>): Set<Task>
    fun linkTask(): List<Class<out Task>>

    fun getGradle():Gradle

    fun <T> getExtends(tClass: Class<T>): T
    fun callBeforeApplyMould()

}
