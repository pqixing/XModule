package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.CleanDialog
import com.pqixing.tools.FileUtils
import groovy.lang.GroovyClassLoader
import java.io.File


class CleanAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")
        if (!projectXmlFile.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(configFile)
        val newInstance = clazz.newInstance()
        var codeRoot = clazz.getField("codeRoot").get(newInstance).toString()
        val codePath = File(basePath, codeRoot).canonicalPath

        //查找出本地所有存在的模块
        val projects = projectXml.allSubModules().map { Pair(it.name, File(codePath, it.path)) }.toMap().filter { it.value.exists() }
        val cleanDialog = CleanDialog(projects.map { JListInfo(it.key, staue = 0, select = true) }.toMutableList())
        val cleanTask = object : Task.Backgroundable(project, "Start Clean") {
            override fun run(indicator: ProgressIndicator) {
                val list = cleanDialog.adapter.datas.filter { it.select }.mapNotNull { projects[it.title] }.toMutableList()
                list.add(0, File(basePath))
                val cleanIdea = cleanDialog.cbIdea.isSelected
                val cleanIml = cleanDialog.cbIdea.isSelected
                list.forEach {
                    indicator.text = "Clean-> ${it.path}"
                    FileUtils.delete(File(it, "build"))
                    if (cleanIdea) FileUtils.delete(File(it, ".idea"))
                    if (cleanIml) FileUtils.delete(File(it, "${it.name}.iml"))
                }
                if (cleanIdea)ApplicationManager.getApplication().invokeLater { ActionManager.getInstance().getAction("InvalidateCaches").actionPerformed(e)}
                else if(cleanIml)ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)
            }
        }
        cleanDialog.setOnOk {
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(cleanTask, BackgroundableProcessIndicator(cleanTask))
        }
        cleanDialog.pack()
        cleanDialog.isVisible = true
    }
}
