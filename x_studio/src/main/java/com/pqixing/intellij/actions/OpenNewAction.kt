package com.pqixing.intellij.actions

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.ui.OpenNewProjectDialog
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.tools.FileUtils
import java.io.File


open class OpenNewAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val defaultProject = e.project ?: ProjectManagerImpl.getInstance().defaultProject?:return
        val rootDir = e.project?.let { LocalFileSystem.getInstance().findFileByPath(it.basePath!!) }
        val showAndPack = OpenNewProjectDialog(defaultProject)
        showAndPack.tvFilePick.addActionListener {
            FileChooser.chooseFiles(FileChooserDescriptor(false, true, false, false, false, false)
                    , defaultProject, rootDir?.parent) { files: List<VirtualFile> ->
                files.firstOrNull()?.let {
                    showAndPack.tvDir.text = it.canonicalPath
                }
            }
        }
        if (rootDir != null) {
            showAndPack.tvDir.text = rootDir.parent.canonicalPath ?: ""
            val tl = File(rootDir.path, EnvKeys.XML_MANIFEST)
//            if (tl.exists()) showAndPack.tvGitUrl.text = XmlHelper.parseManifest(tl).basicUrl
        }

        showAndPack.setOnOk {
            val basicDir = File(showAndPack.tvDir.text.trim(), EnvKeys.BASIC)
            val dir = basicDir.parentFile
            GradleUtils.downloadBasic(defaultProject, basicDir, showAndPack.tvGitUrl.text.trim()) {
                if (rootDir?.exists() == true) {//复制文件
                    FileUtils.copy(File(rootDir.path, "gradle"), File(dir, "gradle"))
                    FileUtils.copy(File(rootDir.path, "gradlew"), File(dir, "gradlew"))
                    FileUtils.copy(File(rootDir.path, "gradlew.bat"), File(dir, "gradlew.bat"))
                    FileUtils.copy(File(rootDir.path, "gradle.properties"), File(dir, "gradle.properties"))
                    FileUtils.copy(File(rootDir.path, "Config.java"), File(dir, "Config.java"))
                }
                FileUtils.writeText(File(dir, "settings.gradle"), "buildscript { apply from: 'https://raw.githubusercontent.com/pqixing/modularization/master/script/install_debug.gradle', to: it }; apply plugin: 'com.module.setting'\n")
                ProjectUtil.openOrImport(dir.absolutePath, defaultProject, true)
            }
        }
        showAndPack.showAndPack()
    }
}
