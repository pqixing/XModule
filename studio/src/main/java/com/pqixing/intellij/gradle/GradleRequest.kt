package com.pqixing.intellij.gradle

import com.android.tools.idea.gradle.task.AndroidGradleTaskManager
import com.intellij.build.BuildViewManager
import com.intellij.build.DefaultBuildDescriptor
import com.intellij.build.events.impl.FinishBuildEventImpl
import com.intellij.build.events.impl.StartBuildEventImpl
import com.intellij.build.events.impl.SuccessResultImpl
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemTaskExecutionEvent
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemEventDispatcher
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.pqixing.tools.UrlUtils
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleExecutionSettings
import org.jetbrains.plugins.gradle.util.GradleConstants

data class GradleRequest(val tasks: List<String>, val env: Map<String, String> = emptyMap(), var visible: Boolean = true) {


    fun getVmOptions(): String {
        val env = mapOf("include" to "Auto", "dependentModel" to "mavenOnly", "buildDir" to "ide", "syncType" to "ide").plus(this.env)

        val option = StringBuilder()
//        if (GradleUtils.debugPort != 0) option.append("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${GradleUtils.debugPort}  -Dorg.gradle.debug=true  --no-daemon ")

        for (it in env.filter { it.key.isNotEmpty() && it.value.isNotEmpty() }) {
            option.append("-D${it.key}=\"${it.value}\" ")
        }
        return option.toString()
    }

    fun runGradle(project: Project, callBack: (r: GradleResult) -> Unit) {
        val taskId = ExternalSystemTaskId.create(GradleConstants.SYSTEM_ID, ExternalSystemTaskType.EXECUTE_TASK, project)
        val parse = GradleParse(project, tasks.firstOrNull()?:"Build",taskId, visible, callBack)
        val setting = GradleExecutionSettings(null, null, DistributionType.BUNDLED, getVmOptions(), false)

        AndroidGradleTaskManager().executeTasks(taskId, tasks, project.basePath!!, setting, null, parse)
    }
}


class GradleParse(val project: Project, val title: String, taskId: ExternalSystemTaskId, val visible: Boolean, val callBack: (r: GradleResult) -> Unit) : ExternalSystemTaskNotificationListenerAdapter() {
    val vm = ServiceManager.getService(project, BuildViewManager::class.java)
    val out: ExternalSystemEventDispatcher? = if (!visible) null else ExternalSystemEventDispatcher(taskId, vm)
    val result: GradleResult = GradleResult()
    override fun onStart(id: ExternalSystemTaskId, workingDir: String?) {
        out?.onEvent(id, StartBuildEventImpl(DefaultBuildDescriptor(id, title, workingDir!!, System.currentTimeMillis()), "running..."))
    }

    override fun onStatusChange(event: ExternalSystemTaskNotificationEvent) {
        if (event is ExternalSystemBuildEvent) {
            out?.onEvent(event.getId(), event.buildEvent)
        } else if (event is ExternalSystemTaskExecutionEvent) {
            out?.onEvent(event.getId(), ExternalSystemUtil.convert(event))
        }
    }

    override fun onEnd(id: ExternalSystemTaskId) {
        out?.onEvent(id, FinishBuildEventImpl(id, null, System.currentTimeMillis(), "finished", SuccessResultImpl()))
        out?.close()
        callBack(result)
    }

    override fun onFailure(id: ExternalSystemTaskId, e: Exception) {
        out?.append(e.message ?: "")
        result.error = e
        result.success = false
    }

    override fun onSuccess(id: ExternalSystemTaskId) {
        result.success = true
    }

    override fun onCancel(id: ExternalSystemTaskId) {
        onFailure(id, RuntimeException("Cancel"))
    }

    override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
        out?.setStdOut(stdOut)
        out?.append(text)
        if (text.startsWith("ide://log")) {
            result.param += UrlUtils.getParams(text)
        }
    }

}

class GradleResult {
    var success: Boolean = false
    var param = mapOf<String, String>()
    var error: Exception? = null

    //获取默认的数据
    fun getDefaultOrNull() = if (success) param["msg"] else null
}