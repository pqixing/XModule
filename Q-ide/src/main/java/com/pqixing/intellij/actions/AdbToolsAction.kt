package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.pqixing.intellij.ui.AdbToolDialog


open class AdbToolsAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        AdbToolDialog(project, e.getData(PlatformDataKeys.VIRTUAL_FILE)?.canonicalPath).showAndPack()
    }
}
