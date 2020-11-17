package com.pqixing.modularization.base

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import java.io.File

interface IPlugin {
    val cacheDir: File
    val buildDir: File
    val projectDir: File
    val rootDir: File

    val project: Project
    fun getTask(taskClass: Class<out Task>): Set<Task>

    fun getGradle(): Gradle
    fun <T> getExtends(tClass: Class<T>): T

}
