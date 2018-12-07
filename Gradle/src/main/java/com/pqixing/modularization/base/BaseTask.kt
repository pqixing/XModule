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
    var ideVersion: String? = null

    init {
        group = Keys.GROUP_TASK
    }

    fun checkIdeVersion() {
//        val ideVersion = TextUtils.getSystemEnv(Keys.ENV_RUN_TYPE)
//        if (ideVersion == null || !ideVersion.contains("ide") || ideVersion.length > minVersion.length || ideVersion >= minVersion)
//            return String
//        errorMsg = "Ide Plugin Update=current plugin version is too low,please update \$ideVersion"
//        Print.lnIde(errorMsg)
//        throw GradleException(errorMsg)

    }

    @TaskAction
    fun run() {
        checkIdeVersion()
        val startTime = System.currentTimeMillis()
        Tools.println("start task $project.name:$name -> ${Date(startTime).toLocaleString()}")
        start()
        runTask()
        end()
        val endTime = System.currentTimeMillis()
        Tools.println("end task $project.name:$name count :  ${endTime - startTime}  :-> ${Date(endTime).toLocaleString()}")
    }


    abstract fun start()

    abstract fun runTask()

    abstract fun end()

    companion object {
        val minVersion = "ide:2.1"

        fun <T : Task> task(project: Project, tClass: Class<T>): T {
            return taskByName(project, getTaskName(tClass), tClass)
        }

        fun <T : Task> taskByName(project: Project, taskName: String, tClass: Class<T>): T {
            return project.tasks.create(taskName,tClass) as T

        }

        fun getTaskName(tClass: Class<*>): String {
            return tClass.simpleName.replace("Task", "")
        }
    }
}
