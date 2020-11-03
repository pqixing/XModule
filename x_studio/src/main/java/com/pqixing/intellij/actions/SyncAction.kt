package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.group.XGroup

class SyncAction : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = XGroup.isBasic(e.project)
    }
    override fun actionPerformed(e: AnActionEvent) {
        ActionManager.getInstance().getAction("Vcs.UpdateProject").actionPerformed(e)
        ApplicationManager.getApplication().executeOnPooledThread { XmlHelper.loadVersionFromNet(e.project?.basePath) }
    }
}