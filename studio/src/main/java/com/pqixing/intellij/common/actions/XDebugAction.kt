package com.pqixing.intellij.common.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.common.XAnAction
import com.pqixing.intellij.common.XGroup

class XDebugAction : XAnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = XGroup.isDebug(e.project)
    }

}