package com.pqixing.modularization.base

import com.pqixing.modularization.ProjectInfo

import org.gradle.api.Project
import org.gradle.api.Task

import java.io.File

interface IPlugin {
    val ignoreFields: Set<String>
    val cacheDir: File
    val buildDir: File
    val projectDir: File
    val rootDir: File

    val project: Project
    val projectInfo: ProjectInfo
    fun getTask(taskClass: Class<out Task>): Set<Task>
    fun linkTask(): List<Class<out Task>>

    fun <T> getExtends(tClass: Class<T>): T

}
