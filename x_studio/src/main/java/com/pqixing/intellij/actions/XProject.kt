package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.pqixing.intellij.ui.BaseJDialog
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

open class XProjectAction : XAnAction() {

    override fun actionPerformed(e: AnActionEvent) {

//        val defaultProject = e.project ?: ProjectManagerImpl.getInstance().defaultProject ?: return
//        val rootDir = e.project?.let { LocalFileSystem.getInstance().findFileByPath(it.basePath!!) }
//        val showAndPack = OpenNewProjectDialog(defaultProject)
//        showAndPack.tvFilePick.addActionListener {
//            showAndPack.isVisible = false
//            FileChooser.chooseFiles(FileChooserDescriptor(false, true, false, false, false, false), defaultProject, rootDir?.parent) { files: List<VirtualFile> ->
//                files.firstOrNull()?.let {
//                    showAndPack.tvDir.text = it.canonicalPath
//                }
//                showAndPack.isVisible = true
//            }
//        }
////        if (rootDir != null) {
//        showAndPack.tvDir.text = rootDir?.parent?.canonicalPath ?: ""
//        showAndPack.tvGitUrl.text = "https://github.com/pqixing/x_basic.git"
////        }
//
//        showAndPack.setOnOk {
//            val basicDir = File(showAndPack.tvDir.text.trim(), EnvKeys.BASIC)
//            val dir = basicDir.parentFile
//
//            GradleUtils.downloadBasic(defaultProject, basicDir, showAndPack.tvGitUrl.text.trim()) {
//                if (rootDir?.exists() == true) {//复制文件
//                    FileUtils.copy(File(rootDir.path, "gradle"), File(dir, "gradle"))
//                    FileUtils.copy(File(rootDir.path, "gradlew"), File(dir, "gradlew"))
//                    FileUtils.copy(File(rootDir.path, "gradlew.bat"), File(dir, "gradlew.bat"))
//                    FileUtils.copy(File(rootDir.path, "gradle.properties"), File(dir, "gradle.properties"))
//                    FileUtils.copy(File(rootDir.path, "Config.java"), File(dir, "Config.java"))
//                }
//                FileUtils.writeText(File(dir, "settings.gradle"), "buildscript { apply from: 'https://gitee.com/pqixing/XModule/raw/master/script/install.gradle', to: it }; apply plugin: 'com.module.setting'\n")
//                ProjectUtil.openOrImport(dir.absolutePath, defaultProject, true)
//            }
//        }
//        showAndPack.showAndPack()
    }
}

open class XProject(project: Project?) : BaseJDialog(project) {
    lateinit var buttonOK: JButton
    lateinit var buttonCancel: JButton
    lateinit var tvDir: JTextField
    lateinit var contentPane: JPanel
    lateinit var tvFilePick: JButton
    lateinit var tvGitUrl: JTextField

    private fun onOK() {
        // add your code here
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        title = "Open New Project"
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    }
}