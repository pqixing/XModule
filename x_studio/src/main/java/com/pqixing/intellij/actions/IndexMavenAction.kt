package com.pqixing.intellij.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.group.XModuleGroup
import com.pqixing.intellij.ui.VersionDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.intellij.utils.GradleUtils.runTask
import com.pqixing.intellij.utils.TaskCallBack
import java.io.File


class IndexMavenAction : AnAction() {
    lateinit var project: Project
    override fun update(e: AnActionEvent) {
        e.presentation?.isEnabledAndVisible = XModuleGroup.hasBasic(e?.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val repo = GitHelper.getRepo(XmlHelper.fileBasic(project.basePath), project) ?: return

        val branches = repo.branches.remoteBranches.map { it.name.substringAfterLast("/") }


        val dialog = VersionDialog(project, branches)
        dialog.pack()
        dialog.isVisible = true

        dialog.setOnOk(Runnable {
            val excludes = dialog.excludes.takeIf { it.isNotEmpty() }?.joinToString(",") ?: ""
            runTask(project, listOf(":IndexMaven"), callback = TaskCallBack { s: Boolean, l: String ->
                if (!s) {
                    Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "IndexMaven", "Gradle Task Error $l", NotificationType.WARNING).notify(project)
                } else ApplicationManager.getApplication().invokeLater { FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(File(l), true)!!, true) }
            })
        })
    }
}
