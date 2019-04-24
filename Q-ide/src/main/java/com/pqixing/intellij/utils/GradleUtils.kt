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
    val defEnvs = mapOf(Pair("include", "Auto"), Pair("dependentModel", "mavenOnly"), Pair("buildDir", "IDE"), Pair("syncType", "ide"))
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
        val env = defEnvs.toMutableMap().apply { putAll(envs);put("run_task_id", runTaskId) }.filter { it.value.isNotEmpty() }
//        settings.env = env
        settings.vmOptions = getVmOpions(env)
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, object : TaskCallback {
            override fun onSuccess() {
                callback?.run()
            }

            override fun onFailure() {
                callback?.run()
            }
        }, progressExecutionMode, activateToolWindowBeforeRun)
    }

    private fun getVmOpions(env: Map<String, String>): String {
        val option = StringBuilder()
        env.forEach {
            if (it.key.isEmpty() || it.value.isEmpty()) return@forEach
            option.append("-D${it.key}='${it.value}' ")
        }
        return option.toString()
    }

    fun getResult(project: Project, runTaskId: String) = getResult(getLogFile(project.basePath!!), runTaskId)

    /**
     * 读取Gradle任务完成的毁掉
     */
    fun getResult(logFile: Array<File>?, runTaskId: String): Pair<Boolean, String> {
        val millis = System.currentTimeMillis()
        if (logFile != null) for (f in logFile) {
            if (!f.exists()) continue
            val lines = f.readLines()
            for (i in lines.size - 1 downTo 0) {
                val params = UrlUtils.getParams(lines[i])
                val taskId = params["run_task_id"]
                val endTime = params["endTime"]?.toLong() ?: 0
                if (millis - endTime > 10000) break//如果任务运行时间,大于结果读取时间10秒钟,则直接判定失败
                if (runTaskId == taskId) return Pair("0" == params["exitCode"], params["msg"] ?: "")
            }
        }
        return Pair(false, "No Result")
    }


    fun getLogFile(basePath: String): Array<File>? {
        val file = File(basePath, ".idea")
        return if (file.exists()) file.listFiles { file, s -> s.contains("modularization.lo") }
        else emptyArray()
    }
}
