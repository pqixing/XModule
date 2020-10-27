package com.pqixing.intellij.utils

import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.google.wireless.android.sdk.stats.GradleSyncStats
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.pqixing.EnvKeys
import com.pqixing.tools.FileUtils
import com.pqixing.tools.UrlUtils
import java.io.File
import java.net.ServerSocket
import java.net.Socket

object GradleUtils {
    val defEnvs = mapOf(Pair("include", "Auto"), Pair("dependentModel", "mavenOnly"), Pair("buildDir", "IDE"), Pair("syncType", "ide"))
    var GRADLE = ProjectSystemId("GRADLE")
    val resultLogs = mutableListOf<String>()//读取的结果
    var serverSocket: ServerSocket? = null
    val defPort: Int = 8451
    var debugPort = 0

    fun runTask(project: Project, tasks: List<String>, progressExecutionMode: ProgressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC, activateToolWindowBeforeRun: Boolean = true, runTaskId: String = System.currentTimeMillis().toString(), envs: Map<String, String> = defEnvs, callback: TaskCallBack? = null) {
        val settings = ExternalSystemTaskExecutionSettings()
        settings.executionName = "Running Task:$tasks"
        settings.taskNames = tasks
        settings.externalSystemIdString = GRADLE.id
        settings.externalProjectPath = project.basePath
        val port = tryInitSocket(defPort)
        val env = defEnvs.toMutableMap().apply { putAll(envs);put("run_task_id", runTaskId);put("ideSocketPort", port.toString()) }.filter { it.value.isNotEmpty() }
        settings.vmOptions = getVmOpions(env)
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, object : TaskCallback {
            override fun onSuccess() {
                if (callback != null) {
                    val result = getResult(project, runTaskId)
                    callback.onTaskEnd(result.first, result.second)
                }
            }

            override fun onFailure() {
                onSuccess()
            }
        }, progressExecutionMode, activateToolWindowBeforeRun)

    }

    fun tryInitSocket(defPort: Int): Int {
        val s = serverSocket?.takeIf { !it.isClosed } ?: createServer(defPort, defPort)
        ?: return defPort
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
                tryNotifyIde(r)
            } else {
                resultLogs.add(r)
                if (resultLogs.size > 20) resultLogs.removeAt(0)
            }
//            System.out.println(Thread.currentThread().name + r)
        }
        accept.close()
    }.start()

    private fun tryNotifyIde(r: String) {
        val params = UrlUtils.getParams(r)
        val projectUrl = UiUtils.base64Decode(params["url"]!!.toString())
        val target = ProjectManager.getInstance().openProjects.find { it.basePath == projectUrl } ?: return

        if (params["type"] == "Miss_${EnvKeys.BASIC}Url") downloadBasic(target, null, null) { GradleSyncInvoker.getInstance().requestProjectSync(target, GradleSyncStats.Trigger.TRIGGER_USER_SYNC_ACTION) }
    }

    fun downloadBasic(target: Project, dir: File?, base: String?, after: () -> Unit) = ApplicationManager.getApplication().invokeLater {
        val url = base
                ?: Messages.showInputDialog("Input your basic git url to init project", "Download Basic", null, "https://github.com/pqixing/md_demo.git", null)?.takeIf { it.isNotEmpty() }
                ?: return@invokeLater
        val basicDir = dir ?: File(target.basePath, EnvKeys.BASIC)
        if (basicDir.exists()) {
            val exitCode = Messages.showOkCancelDialog(target, "basic dir is not empty!!!", "DELETE", "DEL", "CANCEL", null)
            if (exitCode != Messages.OK) return@invokeLater
            FileUtils.delete(basicDir)
        }
        val importTask = object : Task.Backgroundable(target, "Download Basic") {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "clone $url"
                kotlin.runCatching { GitHelper.clone(target, basicDir, url) }
                after()
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

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
        if (debugPort != 0) option.append("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=$debugPort  -Dorg.gradle.debug=true  --no-daemon ")
        env.forEach {
            if (it.key.isEmpty() || it.value.isEmpty()) return@forEach
            option.append("-D${it.key}='${it.value}' ")
        }
        return option.toString()
    }

    fun getResult(project: Project, runTaskId: String) = parseResult(resultLogs.toList(), runTaskId)


    private fun parseResult(logs: List<String>, runTaskId: String): Pair<Boolean, String> {
        val millis = System.currentTimeMillis()
        for (i in logs.size - 1 downTo 0) {
            val params = UrlUtils.getParams(logs[i])
            val taskId = params["run_task_id"]
            val endTime = params["endTime"]?.toLong() ?: 0
            if (millis - endTime > 10000) break//如果任务运行时间,大于结果读取时间10秒钟,则直接判定失败
            if (runTaskId == taskId) return Pair("0" == params["exitCode"], params["msg"] ?: "")
        }
        return Pair(false, "No Result")
    }
}
