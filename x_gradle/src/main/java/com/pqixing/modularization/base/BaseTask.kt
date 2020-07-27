package com.pqixing.modularization.base

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import java.util.*

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

abstract class BaseTask : DefaultTask() {
    open fun prepare() {
        group = Keys.GROUP_TASK
    }

    open fun whenReady() {
    }

    @TaskAction
    fun run() {
        val startTime = System.currentTimeMillis()
        start()
        runTask()
        end()
        val endTime = System.currentTimeMillis()
        Tools.println("Spend :  ${endTime - startTime}")
    }


    open fun start() {}

    abstract fun runTask()

    open fun end() {}

    companion object {

        fun <T : Task> task(project: Project, tClass: Class<T>): T {
            return taskByName(project, getTaskName(tClass), tClass)
        }

        fun <T : Task> taskByName(project: Project, taskName: String, tClass: Class<T>): T {
            return project.tasks.create(taskName, tClass) as T

        }

        fun getTaskName(tClass: Class<*>): String {
            return tClass.simpleName.replace("Task", "")
        }
    }
}
