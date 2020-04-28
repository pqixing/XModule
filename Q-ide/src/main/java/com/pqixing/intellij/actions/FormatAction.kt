package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import java.io.File


open class FormatAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val f = File(project.basePath, ".idea/modules.xml")
        if (f.exists()) ApplicationManager.getApplication().runWriteAction {
            project.save()
            File(project.basePath, ".idea/modules.xml").apply {
                writeText(readText().replace(Regex("group=\".*\""), ""))
            }
            VfsUtil.findFileByIoFile(f,true)?.refresh(false, false)
//            FileEditorManager.getInstance(project).openFile(target!!, true)
        }
    }
}
