package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GitOperatorAction:AnAction() {
    companion object {
        val actionListener = mutableSetOf<Runnable>()
    }
    override fun actionPerformed(e: AnActionEvent) {

    }
}