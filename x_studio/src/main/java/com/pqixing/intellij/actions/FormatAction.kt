package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.intellij.utils.UiUtils
import icons.AndroidIcons
import icons.StudioIcons
import java.io.File


open class FormatAction : AnAction() {

    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        val target = e.project ?: return
        if(Messages.OK != Messages.showOkCancelDialog("Format Module By Group?", "Format Module",Messages.getOkButton(),Messages.getCancelButton(), null)) return
        target.save()
        val moduleFile = VfsUtil.findFileByIoFile(File(target.basePath, ".idea/modules.xml"), true)
        ApplicationManager.getApplication().executeOnPooledThread { UiUtils.formatModule(target,moduleFile,true) }
    }
}
