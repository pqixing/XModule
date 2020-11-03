package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.XGroup
import com.pqixing.intellij.ui.form.XDialog

class DebugAction : XAnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        super.actionPerformed(e)
        XDialog(e.project?:return).show()
//        val project = e.project ?: return
//        GradleRequest(listOf(":clean")).runGradle(project) {
//
//        }
    }


    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = XGroup.isDebug(e.project)
    }

}