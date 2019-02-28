package com.pqixing.intellij.utils

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.pqixing.tools.UrlUtils
import java.io.File

object GradleUtils {
    val defEnvs = mapOf(Pair("include", "Auto"), Pair("dependentModel", "mavenOnly"), Pair("buildDir", "ToMaven"), Pair("syncType", "ide"))
    var GRADLE = ProjectSystemId("GRADLE")

    fun runTask(project: Project
                , tasks: List<String>
                , progressExecutionMode: ProgressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC
                , activateToolWindowBeforeRun: Boolean = true
                , runTaskId: String = System.currentTimeMillis().toString()
                , envs: Map<String, String> = defEnvs
                , callback: Runnable? = null) {

        val settings = ExternalSystemTaskExecutionSettings()
        settings.executionName = "Running Task:$tasks"
        settings.taskNames = tasks
        settings.externalSystemIdString = GRADLE.id
        settings.externalProjectPath = project.basePath
        settings.env = envs.toMutableMap().apply { put("run_task_id", runTaskId) }
        settings.vmOptions = getVmOpions(settings.env)
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, object : TaskCallback {
            override fun onSuccess() {
                callback?.run()
            }

            override fun onFailure() {
                callback?.run()
            }
        }, progressExecutionMode, activateToolWindowBeforeRun)
    }

    private fun getVmOpions(env: MutableMap<String, String>): String {
        val option = StringBuilder()
        env.forEach {
            option.append("-D${it.key}=${it.value} ")
        }
        return option.toString()
    }

    /**
     * 读取Gradle任务完成的毁掉
     */
    fun getResult(logFile: File, runTaskId: String): Pair<Boolean, String> {
        if (!logFile.exists()) return Pair(false, "No Result")
        val lines = logFile.readLines()
        val millis = System.currentTimeMillis()
        for (i in lines.size - 1 downTo 0) {
            val params = UrlUtils.getParams(lines[i])
            val taskId = params["run_task_id"]
            val endTime = params["endTime"]?.toLong() ?: 0
            if (millis - endTime > 10000) return Pair(false, "No Result")//如果任务运行时间,大于结果读取时间10秒钟,则直接判定失败
            if (runTaskId != taskId) continue
            return Pair("0" == params["exitCode"], params["msg"] ?: "")
        }
        return Pair(false, "No Result")
    }

    fun getLogFile(basePath: String) = File(basePath, ".idea/modularization.log")
}
