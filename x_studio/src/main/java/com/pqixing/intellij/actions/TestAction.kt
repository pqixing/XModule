package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.gradle.GradleRequest

class TestAction : XAnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        super.actionPerformed(e)
//        XDialog(e.project?:return).show()
        val project = e.project ?: return
        GradleRequest(listOf(":clean")).runGradle(project) {

        }
    }

}