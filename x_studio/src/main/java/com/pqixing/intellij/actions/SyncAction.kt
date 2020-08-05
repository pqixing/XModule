package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.pqixing.help.XmlHelper

class SyncAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ActionManager.getInstance().getAction("Vcs.UpdateProject").actionPerformed(e)
        ApplicationManager.getApplication().executeOnPooledThread { XmlHelper.loadVersionFromNet(e.project?.basePath) }
    }
}