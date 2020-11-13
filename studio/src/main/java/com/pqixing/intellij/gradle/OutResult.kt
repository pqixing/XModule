package com.pqixing.intellij.gradle

import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemTaskExecutionEvent
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemEventDispatcher
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.pqixing.intellij.XApp

class OutResult(project: Project, taskId: ExternalSystemTaskId, val visible: Boolean) {
    val out = if (!visible) null else kotlin.runCatching { ExternalSystemEventDispatcher(taskId, ServiceManager.getService(project, BuildViewManager::class.java)) }.getOrNull()


    fun onEvent(buildId: Any, event: BuildEvent) {
        try {

            out?.onEvent(buildId, event)
        } finally {

        }
    }

    fun onEvent(buildId: Any, event: ExternalSystemTaskNotificationEvent) {
        try {
            if (event is ExternalSystemBuildEvent) {
                onEvent(event.getId(), event.buildEvent)
            } else if (event is ExternalSystemTaskExecutionEvent) {
                onEvent(event.getId(), ExternalSystemUtil.convert(event))
            }
        } finally {

        }
    }

    fun close() {
        kotlin.runCatching {
            out?.close()
        }
    }

    fun append(csq: CharSequence?, stdOut: Boolean? = null) {
        try {
            if (stdOut != null) out?.setStdOut(stdOut)
            if (visible) out?.append(csq)
        } catch (e: java.lang.Exception) {
            XApp.log(csq?.toString())
        }
    }
}