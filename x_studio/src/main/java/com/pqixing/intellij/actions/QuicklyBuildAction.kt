package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.ui.BuilderDialog

open class QuicklyBuildAction : AnAction() {
    companion object {
        val quicklyTask = mutableListOf<QuicklyParam>()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project?:return
//        BuilderDialog(project, getConfigInfo(configFile), getActivityModules(e, allSubModule), allSubModule, getBranches()).showAndPack()
//
//        writeLocalBuild(allMap)
//        isVisible = false
//        startLocalBuild(0, allMap, iDevice)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = quicklyTask.find { it.projectPath == e.project?.basePath ?: "" } != null
    }
}

class QuicklyParam {
    var deviceId: String = ""
    var projectPath = ""
    var params: List<Map<String, String>> = emptyList()
}