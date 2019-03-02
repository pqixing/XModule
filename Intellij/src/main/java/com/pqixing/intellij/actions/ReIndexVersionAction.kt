package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.ui.VersionDialog
import com.pqixing.intellij.utils.GradleUtils
import java.util.*


class ReIndexVersionAction : AnAction() {
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val exitCode = Messages.showYesNoCancelDialog("CreateVersionTag", "ReIndexVersion", null)
        if (exitCode == 0){
            val dialog = VersionDialog(project, null)
            dialog.pack()
            dialog.isVisible = true
        }
        else if (exitCode == 1) GradleUtils.runTask(project, Arrays.asList(":VersionIndex"))

    }
}
