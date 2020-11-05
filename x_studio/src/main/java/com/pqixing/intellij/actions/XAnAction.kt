package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.pqixing.intellij.XGroup.Companion.isBasic
import com.pqixing.intellij.ui.weight.XEventDialog

abstract class XEventAction(val block: (project: Project, e: AnActionEvent, module: Module?) -> XEventDialog?) : XAnAction() {
    override fun doPerformed(project: Project, e: AnActionEvent, module: Module?) {
        block(project, e, module)?.show()
    }
}

open class XAnAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = isBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        doPerformed(project, e, e.getData(DataKey.create<Module>("module")))
    }

    protected open fun doPerformed(project: Project, e: AnActionEvent, module: Module?) {}
}