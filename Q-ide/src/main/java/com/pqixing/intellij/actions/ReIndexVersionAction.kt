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
    override fun update(e: AnActionEvent?) {
        e?.presentation?.isEnabledAndVisible = QToolGroup.isModulariztionProject(e?.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val exitCode = Messages.showCheckboxMessageDialog("Sync Version From Net For All Module               "
                , "Sync Version"
                , arrayOf("Sync")
                , "Tag For Branch"
                , false
                , 0
                , -1
                , null) { i, c -> if (i == Messages.YES && c.isSelected) 4 else i }
        if (exitCode == 4) VersionDialog(project).apply {
            pack()
            isVisible = true
        } else if (exitCode == Messages.YES) GradleUtils.runTask(project, Arrays.asList(":VersionIndex"))
    }
}
