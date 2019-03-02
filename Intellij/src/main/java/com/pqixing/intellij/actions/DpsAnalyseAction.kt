package com.pqixing.intellij.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.intellij.utils.GradleUtils
import java.io.File


class DpsAnalyseAction : AnAction() {
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(DataKey.create<Module>("module"))
        val moduleName = module?.name ?: ""

        val projectMode = module == null || project.name.replace(" ", "") == moduleName;
        if (projectMode) return
        val runTaskId = System.currentTimeMillis().toString()
        val callBack = Runnable {
            val result = GradleUtils.getResult(project, runTaskId)
            if (!result.first) {
                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "DpsAnalyse", "Gradle Task Error ${result.second}", NotificationType.WARNING).notify(project)
                return@Runnable
            }
            ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(File(result.second), true)!!, true)
            }
        }
        GradleUtils.runTask(project, listOf(":$moduleName:DpsAnalysis"), activateToolWindowBeforeRun = true, runTaskId = runTaskId, callback = callBack
                , envs = mapOf(Pair("include", ""), Pair("dependentModel", "")))
    }
}
