package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.XGroup

class XDebugAction : XAnAction() {
    override fun update(e: AnActionEvent) {
        XGroup.isDebug(e.project)
    }
}