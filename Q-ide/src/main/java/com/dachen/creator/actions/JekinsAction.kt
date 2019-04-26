package com.dachen.creator.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.ui.JekinJobDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.model.SubModuleType
import com.pqixing.tools.PropertiesUtils
import java.io.File

open class JekinsAction : AnAction() {
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
        val module = e.getData(DataKey.create<Module>("module"))?.name
        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val apps = projectXml.allSubModules().filter { it.type == SubModuleType.TYPE_APPLICATION }.map { it.name }.toMutableList()
        if (module != null&&apps.remove(module)) {
            apps.add(0, module)
        }
        val repo = GitHelper.getRepo(File(basePath, "templet"), project)
        val branchs = repo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }.toMutableList()
        val localBranch = repo.currentBranchName ?: "master"
        branchs.remove(localBranch)
        branchs.add(0, localBranch)
        val dialog = JekinJobDialog(project, apps, branchs)
        dialog.pack()
        dialog.isVisible = true
    }
}