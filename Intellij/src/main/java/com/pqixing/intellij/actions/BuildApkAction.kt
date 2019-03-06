package com.pqixing.intellij.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.ui.InstallApkDialog
import com.pqixing.intellij.utils.GradleUtils


open class BuildApkAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(DataKey.create<Module>("module"))
        val moduleName = module?.name ?: ""

        val projectMode = /*"ProjectViewPopup".equals(place)||*/"MainMenu" == e.place || module == null || project.name.replace(" ","") == moduleName;

        if (projectMode||Messages.showOkCancelDialog("Build $moduleName before install?","Build Apk",null)!=0) {
            val apkDialog = InstallApkDialog(e.project, e.project!!.basePath+"/build/apks")
            apkDialog.pack()
            apkDialog.isVisible = true
            return
        }
        val runTaskId = System.currentTimeMillis().toString()

        val callBack = Runnable {
            val result = GradleUtils.getResult(GradleUtils.getLogFile(project.basePath!!), runTaskId)
            if (!result.first) {
                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "BuildApk", "Build $moduleName Apk Fail !!", NotificationType.WARNING).notify(project)
                return@Runnable
            }
            val apkDialog = InstallApkDialog(project, result.second)
            apkDialog.pack()
            apkDialog.isVisible = true
//            AdbShellCommandsUtil.executeCommand()
        }
        GradleUtils.runTask(project, listOf(":$moduleName:PrepareDev", ":$moduleName:BuildApk"), activateToolWindowBeforeRun = true, runTaskId = runTaskId, callback = callBack,envs = mapOf(Pair("include",""), Pair("dependentModel","")))
    }
}
