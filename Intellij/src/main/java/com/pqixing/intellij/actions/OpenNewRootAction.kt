package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project


class OpenNewRootAction : AnAction() {
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(DataKey.create<Module>("module"))
        val moduleName = module?.name ?: ""

        val projectMode = /*"ProjectViewPopup".equals(place)||*/"MainMenu" == e.place || module == null || project.name == moduleName;

    }
}
