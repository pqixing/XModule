package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XGroup
import com.pqixing.intellij.ui.CleanDialog
import com.pqixing.intellij.utils.UiUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import java.io.File


class CleanAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun update(e: AnActionEvent) {
        e?.presentation?.isEnabledAndVisible = XGroup.isBasic(e?.project)
    }
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return

        val projectXml = XmlHelper.loadManifest(basePath)?:return
        val config = XmlHelper.loadConfig(basePath)
        val codeRoot = config.toString().trim()
        val properties = PropertiesUtils.readProperties(File(project.basePath, UiUtils.IDE_PROPERTIES))

        val codeRoots = properties.getProperty("codeRoots", "").split(",").filter { it.isNotEmpty() }.toMutableList()
        codeRoots.remove(codeRoot)
        codeRoots.add(0, codeRoot)
        //查找出本地所有存在的模块
        val cleanDialog = CleanDialog(project, codeRoots, projectXml.projects.map { it.path }, projectXml.allModules().map { it.path })
        val cleanTask = object : Task.Backgroundable(project, "Start Clean") {
            override fun run(indicator: ProgressIndicator) {

                val selectRoot = cleanDialog.cbCodeRoots!!.selectedItem.toString().trim()
                val codePath = File(basePath, selectRoot)

                val cleanCache = cleanDialog.cbCache!!.isSelected
                val cleanIde = cleanDialog.cbIml!!.isSelected
                val restart = selectRoot == codeRoot && (cleanIde || !cleanCache)
                var returnRun = false
                ApplicationManager.getApplication().invokeAndWait {
                    returnRun = restart && Messages.showYesNoDialog(project, "this clean request restart!!!", "Warming", null) != Messages.YES
                }
                if (returnRun) return

                if (cleanCache && selectRoot == codeRoot) {
                    clear(indicator, File(basePath), cleanCache, cleanIde)
                }
                cleanDialog.adapter.datas.filter { it.select }.map { File(codePath, it.title) }.forEach {
                    clear(indicator, it, cleanCache, cleanIde)
                }
                //如果codeRoot目录为空,删掉
                if (!cleanCache && codePath.listFiles().isEmpty()) {
                    FileUtils.delete(codePath)
                }

                if (restart) (ApplicationManager.getApplication() as ApplicationImpl).restart(true)
                else ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)
            }
        }
        cleanDialog.onOk = Runnable {
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(cleanTask, BackgroundableProcessIndicator(cleanTask))
        }
        cleanDialog.pack()
        cleanDialog.isVisible = true
    }

    fun clear(indicator: ProgressIndicator, dir: File, cleanCeche: Boolean, cleanIde: Boolean) {
        indicator.text = "Clean-> ${dir.absolutePath}"
        if (!cleanCeche) FileUtils.delete(dir)
        else {
            FileUtils.delete(File(dir, "build"))
            if (cleanIde) {
                FileUtils.delete(File(dir, ".idea"))
                FileUtils.delete(File(dir, ".gradle"))
                FileUtils.delete(File(dir, "${dir.name}.iml"))
            }
        }
    }
}
