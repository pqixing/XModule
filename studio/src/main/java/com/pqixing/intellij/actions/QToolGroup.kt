package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.UiUtils
import java.io.File

class QToolGroup : DefaultActionGroup() {

    companion object {
        init {
            //启动后，尝试打开socket连接，接收gradle插件的通知
            GradleUtils.tryInitSocket(GradleUtils.defPort)
            UiUtils.checkIfFormat(null)
        }

        fun isModulariztionProject(project: Project?): Boolean = File(project?.basePath,"templet/project.xml").exists()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        //启动后，尝试打开socket连接，接收gradle插件的通知
        GradleUtils.tryInitSocket(GradleUtils.defPort)
    }
}
