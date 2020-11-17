package com.pqixing.modularization.base

import com.pqixing.help.Tools
import com.pqixing.modularization.Keys
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

abstract class BaseTask : DefaultTask() {

    init {
        if (matchTask()) initByUserExe()
    }

    protected open fun initByUserExe() {

    }

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

    public open fun matchTask(): Boolean = Companion.matchTask(listOf(name), project.gradle.startParameter.taskNames)

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

        fun matchTask(task: Class<*>, taskNames: List<String>): Boolean = matchTask(listOf(getTaskName(task)),taskNames)
        fun matchTask(keys: List<String>, taskNames: List<String>): Boolean {
            if (keys.isEmpty()) return false
            for (key in keys) {
                for (task in taskNames) {
                    var k = 0
                    var l = -1
                    while (++l < task.length) if (key[k] == task[l] && ++k == key.length) return true
                }
            }
            return false
        }
    }
}
