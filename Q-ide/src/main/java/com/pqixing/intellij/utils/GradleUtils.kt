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
import java.net.ServerSocket
import java.net.Socket

object GradleUtils {
    val defEnvs = mapOf(Pair("include", "Auto"), Pair("dependentModel", "mavenOnly"), Pair("buildDir", "IDE"), Pair("syncType", "ide"))
    var GRADLE = ProjectSystemId("GRADLE")
    val resultLogs = mutableListOf<String>()//读取的结果
    var serverSocket: ServerSocket? = null

    fun runTask(project: Project
                , tasks: List<String>
                , progressExecutionMode: ProgressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC
                , activateToolWindowBeforeRun: Boolean = true
                , runTaskId: String = System.currentTimeMillis().toString()
                , envs: Map<String, String> = defEnvs
                , callback: GradleTaskCallBack? = null) {
        val settings = ExternalSystemTaskExecutionSettings()
        settings.executionName = "Running Task:$tasks"
        settings.taskNames = tasks
        settings.externalSystemIdString = GRADLE.id
        settings.externalProjectPath = project.basePath
        val port = initSocket()
        val env = defEnvs.toMutableMap().apply { putAll(envs);put("run_task_id", runTaskId);put("ideSocketPort", port.toString()) }.filter { it.value.isNotEmpty() }
//        settings.env = env
        settings.vmOptions = getVmOpions(env)
        initLogFile(project)
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, object : TaskCallback {
            override fun onSuccess() {
                if (callback != null) {
                    val result = GradleUtils.getResult(project, runTaskId)
                    callback.onTaskEnd(result?.first, result?.second)
                }
            }

            override fun onFailure() {
                onSuccess()
            }
        }, progressExecutionMode, activateToolWindowBeforeRun)

    }

    private fun initLogFile(project: Project) {
        val logFile = File(project.basePath, ".idea/modularization.log")
        if (!logFile.exists()) {
            logFile.parentFile.mkdirs()
            logFile.createNewFile()
        }
    }

    private fun initSocket(): Int {
        val s = serverSocket ?: createServer(8890)?:return 8890
        if (serverSocket == null) {
            serverSocket = s
            Thread {
                while (true) {
                    acceptNewInput(s.accept())
                }
            }.start()
        }
        return s.localPort
    }

    private fun acceptNewInput(accept: Socket) = Thread {
        val inputStream = accept.getInputStream().bufferedReader()
        while (true) {
            val r = inputStream.readLine() ?: break
            resultLogs.add(r)
            if(resultLogs.size>20) resultLogs.removeAt(0)

            System.out.println(Thread.currentThread().name + r)
        }
        accept.close()
    }.start()

    /**
     * 绑定新的端口
     */
    private fun createServer(port: Int): ServerSocket? = if(port>10000) null  else try {
        ServerSocket(port)
    } catch (e: Exception) {
        createServer(port + 1)
    }


    private fun getVmOpions(env: Map<String, String>): String {
        val option = StringBuilder()
        env.forEach {
            if (it.key.isEmpty() || it.value.isEmpty()) return@forEach
            option.append("-D${it.key}='${it.value}' ")
        }
        return option.toString()
    }

    fun getResult(project: Project, runTaskId: String) = parseResult(resultLogs.toList(), runTaskId)
            ?: getResult(getLogFile(project.basePath!!), runTaskId)

    /**
     * 读取Gradle任务完成的毁掉
     */
    private fun getResult(logFile: Array<File>?, runTaskId: String): Pair<Boolean, String> {
        if (logFile != null) for (f in logFile) {
            if (!f.exists()) continue
            return parseResult(f.readLines(), runTaskId) ?: continue
        }
        return Pair(false, "No Result")
    }

    private fun parseResult(logs: List<String>, runTaskId: String): Pair<Boolean, String>? {
        val millis = System.currentTimeMillis()
        for (i in logs.size - 1 downTo 0) {
            val params = UrlUtils.getParams(logs[i])
            val taskId = params["run_task_id"]
            val endTime = params["endTime"]?.toLong() ?: 0
            if (millis - endTime > 10000) break//如果任务运行时间,大于结果读取时间10秒钟,则直接判定失败
            if (runTaskId == taskId) return Pair("0" == params["exitCode"], params["msg"] ?: "")
        }
        return null
    }


    fun getLogFile(basePath: String): Array<File>? {
        val file = File(basePath, ".idea")
        return if (file.exists()) file.listFiles { file, s -> s.contains("modularization.log") }
        else emptyArray()
    }
}
