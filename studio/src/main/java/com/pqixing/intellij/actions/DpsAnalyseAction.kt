package com.pqixing.intellij.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.intellij.group.QToolGroup
import com.pqixing.intellij.utils.TaskCallBack
import com.pqixing.intellij.utils.GradleUtils
import java.io.File


class DpsAnalyseAction : AnAction() {
    lateinit var project: Project
    override fun update(e: AnActionEvent) {
        e?.presentation?.isEnabledAndVisible = QToolGroup.hasBasic(e?.project) && e?.getData(LangDataKeys.MODULE) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(LangDataKeys.MODULE)
        val moduleName = module?.name ?: ""

        val projectMode = module == null || project.name.replace(" ", "") == moduleName;
        if (projectMode) return
        val runTaskId = System.currentTimeMillis().toString()
        val callBack = TaskCallBack { result, log ->
            if (!result) {
                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "DpsAnalyse", "Gradle Task Error ${log}", NotificationType.WARNING).notify(project)
            } else ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(File(log), true)!!, true)
            }
        }
        GradleUtils.runTask(project, listOf(":$moduleName:DpsAnalysis"), activateToolWindowBeforeRun = true, runTaskId = runTaskId, callback = callBack
                , envs = mapOf(Pair("include", ""), Pair("dependentModel", "")))
    }
}
