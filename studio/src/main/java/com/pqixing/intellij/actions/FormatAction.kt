package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.intellij.utils.UiUtils
import icons.AndroidIcons
import icons.StudioIcons
import java.io.File


open class FormatAction : AnAction() {

    lateinit var project: Project
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.icon = AndroidIcons.Android
//        e.presentation.isEnabledAndVisible = false
    }
    override fun actionPerformed(e: AnActionEvent) {
        val target = e.project ?: return
        target.save()
        val moduleFile = VfsUtil.findFileByIoFile(File(target.basePath, ".idea/modules.xml"), true)
        UiUtils.addTask(100, Runnable { UiUtils.formatModule(target,moduleFile,true) })

    }
}
