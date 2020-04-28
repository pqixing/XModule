package com.pqixing.intellij.actions

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.openapi.vfs.VirtualFile
import com.pqixing.intellij.ui.OpenNewProjectDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.tools.FileUtils
import java.io.File


open class OpenNewAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {

        val defaultProject = ProjectManagerImpl.getInstance().defaultProject
        val p = e.project
        val showAndPack = OpenNewProjectDialog()
        showAndPack.tvFilePick.addActionListener {
            FileChooser.chooseFiles( FileChooserDescriptor(false,true,false,false,false,false)
                    , defaultProject, p?.baseDir?.parent) { files: List<VirtualFile> ->
                files.firstOrNull()?.let {
                    showAndPack.tvDir.text = it.canonicalPath
                }
            }
        }
        if(p!=null)  showAndPack.tvDir.text = p.baseDir.parent.canonicalPath
        showAndPack.setOnOk {
            var dir = File(showAndPack.tvDir.text.trim(), "CRoot")
            var i = 0
            while (dir.exists()) dir = File(showAndPack.tvDir.text.trim(), "CRoot${i++}")
            dir.mkdirs()

            val gitUrl = showAndPack.tvGitUrl.text.trim()
            val doClone = GitHelper.getGit().clone(defaultProject, dir, gitUrl, "templet").exitCode == 0
            FileUtils.copy(File(dir, "templet/build.gradle"), File(dir, "build.gradle"))
            FileUtils.copy(File(dir, "templet/Config.java"), File(dir, "Config.java"))
            if (doClone){
                ProjectUtil.openOrImport(File(dir, "build.gradle").absolutePath, p?:defaultProject, true)
            }
        }
        showAndPack.showAndPack()
    }
}
