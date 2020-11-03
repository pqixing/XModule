package com.pqixing.creator.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XGroup
import com.pqixing.intellij.ui.BuilderDialog
import com.pqixing.intellij.git.uitils.GitHelper
import com.pqixing.intellij.utils.UiUtils.realName
import groovy.lang.GroovyClassLoader
import java.io.File

open class BuilderAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation?.isVisible = XGroup.isBasic(e?.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val allSubModule = XmlHelper.loadAllModule(basePath)

        BuilderDialog(project, XmlHelper.loadConfig(basePath), getActivityModules(e, allSubModule), allSubModule, getBranches()).showAndPack()
    }

    private fun getConfigInfo(configFile: File) = GroovyClassLoader().parseClass(configFile).newInstance()

    private fun getActivityModules(e: AnActionEvent, allModule: MutableSet<com.pqixing.model.Module>): MutableList<String> {
        val curModule = e.getData(DataKey.create<Module>("module"))?.realName()
        val moduleNames = allModule.filter { it.isAndroid }.map { it.name }
        val activityModules = ModuleManager.getInstance(project).sortedModules.filter { moduleNames.contains(it.name) }.mapNotNull { it.name }.toMutableList()
        if (curModule != null) {
            activityModules.remove(curModule)
            activityModules.add(0, curModule)
        }
        return activityModules
    }

    private fun getBranches(): MutableList<String> {
        val repo = GitHelper.getRepo(File(basePath, EnvKeys.BASIC), project)
        val branchs = repo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }.toMutableList()
        val localBranch = repo.currentBranchName ?: "master"
        branchs.remove(localBranch)
        branchs.add(0, localBranch)
        return branchs
    }
}