package com.pqixing.intellij.common.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.common.XAnAction
import com.pqixing.intellij.common.XGroup
import com.pqixing.intellij.git.uitils.GitHelper
import git4idea.commands.Git
import java.io.File

class XDebugAction : XAnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = XGroup.isDebug(e.project)
        Git.getInstance().clone(e.project!!, File("/Users/pqx/Desktop/px"),"https://github.com/pqixing/px.git","px",null)
    }

}