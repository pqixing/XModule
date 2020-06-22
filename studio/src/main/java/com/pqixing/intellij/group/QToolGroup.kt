package com.pqixing.intellij.group

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.pqixing.EnvKeys
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

        fun hasBasic(project: Project?): Boolean = File(project?.basePath, EnvKeys.XML_PROJECT).exists()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        //启动后，尝试打开socket连接，接收gradle插件的通知
        GradleUtils.tryInitSocket(GradleUtils.defPort)
    }
}
