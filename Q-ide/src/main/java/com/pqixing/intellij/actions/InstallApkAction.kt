package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.BuildParamDialog
import com.pqixing.intellij.ui.NewInstallDialog
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.IInstall
import com.pqixing.intellij.utils.IInstallListener


open class InstallApkAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val moduleName = e.getData(DataKey.create<Module>("module"))?.name
        var param:BuildParamDialog?=null
        val install = IInstall { info,index, dialog, indicator, c ->
            if(index==0){
                if( dialog.cbBuildParams?.isSelected==true) ApplicationManager.getApplication().invokeAndWait {
                    param = BuildParamDialog()
                    param?.pack()
                    param?.isVisible = true
                }else param = null
            }
            indicator.text = "Building ${info.title}"
            GradleUtils.runTask(project, listOf(":${info.title}:PrepareDev", ":${info.title}:BuildApk"), activateToolWindowBeforeRun = true
                    , envs = mapOf(Pair("include", ""), Pair("dependentModel", "dpMode")), callback = c)
        }
        val modulesName = ModuleManager.getInstance(project).modules.filter { it.name != project.name }.map { it.name }
        val modules = modulesName.map { JListInfo(it, select = it == moduleName).apply { data = install } }
        val apkDialog = NewInstallDialog(project
                , e.getData(PlatformDataKeys.VIRTUAL_FILE)?.canonicalPath, moduleName == null || !modulesName.contains(moduleName)
                , modules)
        apkDialog.pack()
        apkDialog.isVisible = true
    }
}
