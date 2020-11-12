package com.pqixing.intellij.common.actions

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.common.XEventAction
import com.pqixing.intellij.gradle.GradleUtils
import com.pqixing.intellij.ui.weight.XEventDialog
import com.pqixing.tools.FileUtils
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

open class XProjectAction : XEventAction<XProjectDialog>() {
    override fun update(e: AnActionEvent) {
    }
}

open class XProjectDialog(e: AnActionEvent) : XEventDialog(e, e.project ?: ProjectManagerImpl.getInstance().defaultProject) {
    lateinit var tvDir: JTextField
    lateinit var centerPanal: JPanel
    lateinit var tvFilePick: JButton
    lateinit var tvGitUrl: JTextField
    val rootDir = LocalFileSystem.getInstance().findFileByPath(basePath)

    override fun init() {
        super.init()
        cbAll.isVisible = false
        tvFilePick.addActionListener {
            FileChooser.chooseFiles(FileChooserDescriptor(false, true, false, false, false, false), project, rootDir?.parent) { files: List<VirtualFile> ->
                files.firstOrNull()?.let { tvDir.text = it.canonicalPath }
            }
        }
        tvDir.text = rootDir?.parent?.canonicalPath ?: ""
        tvGitUrl.text = XmlHelper.loadManifest(basePath)?.basicUrl?.takeIf { it.isNotEmpty() } ?: "https://github.com/pqixing/basic.git"
    }

    override fun getTitleStr(): String = "Open X Project"
    override fun doOKAction() {
        super.doOKAction()
        val basicDir = File(tvDir.text.trim(), EnvKeys.BASIC)
        val dir = basicDir.parentFile

        GradleUtils.downloadBasic(project, basicDir, tvGitUrl.text.trim()) {
            if (rootDir?.exists() == true) {//复制文件
                FileUtils.copy(File(rootDir.path, "gradle"), File(dir, "gradle"))
                FileUtils.copy(File(rootDir.path, "gradlew"), File(dir, "gradlew"))
                FileUtils.copy(File(rootDir.path, "gradlew.bat"), File(dir, "gradlew.bat"))
                FileUtils.copy(File(rootDir.path, "gradle.properties"), File(dir, "gradle.properties"))
//                FileUtils.copy(File(rootDir.path, "Config.java"), File(dir, "Config.java"))
            }
            FileUtils.writeText(File(dir, "settings.gradle"), "buildscript { apply from: 'https://github.com/pqixing/cmmon/raw/master/script/install.gradle', to: it }; apply plugin: 'com.module.setting'\n")
            ProjectUtil.openOrImport(dir.absolutePath, project, true)
        }
    }


    override fun createCenterPanel(): JComponent = centerPanal
}