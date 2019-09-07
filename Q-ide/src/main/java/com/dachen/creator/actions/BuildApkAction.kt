package com.dachen.creator.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.actions.QToolGroup
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.BuildApkDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.model.SubModule
import com.pqixing.model.SubModuleType
import groovy.lang.GroovyClassLoader
import java.io.File

open class BuildApkAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun update(e: AnActionEvent?) {
        super.update(e)
        e?.presentation?.isVisible = QToolGroup.isDachenProject(e?.project)
    }
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")


        if (!projectXmlFile.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val allSubModule = XmlHelper.parseProjectXml(projectXmlFile).allSubModules()
        BuildApkDialog(project, getConfigInfo(configFile), getActivityModules(e, allSubModule), allSubModule, getBranches()).showAndPack()
    }

    private fun getConfigInfo(configFile: File) = GroovyClassLoader().parseClass(configFile).newInstance()

    private fun getActivityModules(e: AnActionEvent, allSubModule: MutableSet<SubModule>): MutableList<String> {
        val curModule = e.getData(DataKey.create<Module>("module"))?.name
        val moduleNames = allSubModule.map { it.name }
        val activityModules = ModuleManager.getInstance(project).sortedModules.filter { moduleNames.contains(it.name) }.mapNotNull { it.name }.toMutableList()
        if (curModule != null) {
            activityModules.remove(curModule)
            activityModules.add(0, curModule)
        }
        return activityModules
    }

    private fun getBranches(): MutableList<String> {
        val repo = GitHelper.getRepo(File(basePath, "templet"), project)
        val branchs = repo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }.toMutableList()
        val localBranch = repo.currentBranchName ?: "master"
        branchs.remove(localBranch)
        branchs.add(0, localBranch)
        return branchs
    }
}