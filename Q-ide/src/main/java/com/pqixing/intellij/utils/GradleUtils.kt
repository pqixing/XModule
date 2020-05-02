package com.pqixing.intellij.utils

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.tools.UrlUtils
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

object GradleUtils {
    val defEnvs = mapOf(Pair("include", "Auto"), Pair("dependentModel", "mavenOnly"), Pair("buildDir", "IDE"), Pair("syncType", "ide"))
    var GRADLE = ProjectSystemId("GRADLE")
    val resultLogs = mutableListOf<String>()//读取的结果
    var serverSocket: ServerSocket? = null
    val defPort: Int = 8451

    fun runTask(project: Project
                , tasks: List<String>
                , progressExecutionMode: ProgressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC
                , activateToolWindowBeforeRun: Boolean = true
                , runTaskId: String = System.currentTimeMillis().toString()
                , envs: Map<String, String> = defEnvs
                , callback: TaskCallBack? = null) {
        val settings = ExternalSystemTaskExecutionSettings()
        settings.executionName = "Running Task:$tasks"
        settings.taskNames = tasks
        settings.externalSystemIdString = GRADLE.id
        settings.externalProjectPath = project.basePath
        val port = tryInitSocket(defPort)
        val env = defEnvs.toMutableMap().apply { putAll(envs);put("run_task_id", runTaskId);put("ideSocketPort", port.toString()) }.filter { it.value.isNotEmpty() }
//        settings.env = env
        settings.vmOptions = getVmOpions(env)
//        initLogFile(project)
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

    fun tryInitSocket(defPort: Int): Int {
        val s = serverSocket?.takeIf { !it.isClosed } ?: createServer(defPort, defPort) ?: return defPort
        if (s != serverSocket) Thread {
            while (!s.isClosed) {
                acceptNewInput(s.accept())
            }
        }.start()
        serverSocket = s
        return s.localPort
    }

    private fun acceptNewInput(accept: Socket) = Thread {
        val inputStream = accept.getInputStream().bufferedReader()

        while (accept.isConnected) {
            val r = inputStream.readLine() ?: break
            if (r.startsWith("ide://notify")) {//通知ide刷新
                val params = UrlUtils.getParams(r)
                if (params["type"] == "buildFinished") {
                    val projectUrl = UiUtils.base64Decode(params["url"]!!.toString())
                    val target = ProjectManager.getInstance().openProjects.find { it.basePath == projectUrl }
                    if (target != null) {
                        Thread.sleep(2222)
                        UiUtils.formatProject(target)
                    }
                }
            } else {
                resultLogs.add(r)
                if (resultLogs.size > 20) resultLogs.removeAt(0)
            }
//            System.out.println(Thread.currentThread().name + r)
        }
        accept.close()
    }.start()

    /**
     * 绑定新的端口
     */
    private fun createServer(first: Int, port: Int): ServerSocket? = if (port - first > 20) null else try {
        ServerSocket(port)
    } catch (e: Exception) {
        e.printStackTrace()
        createServer(first, port + 1)
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
            ?: getResult(project, getLogFile(project.basePath!!), runTaskId)

    /**
     * 读取Gradle任务完成的毁掉
     */
    private fun getResult(project: Project, logFile: Array<File>?, runTaskId: String): Pair<Boolean, String> {
        project.save()
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
        val file = File(basePath, ".idea/caches")
        return if (file.exists()) file.listFiles { _, s -> s.contains("task.log") }
        else emptyArray()
    }
}
