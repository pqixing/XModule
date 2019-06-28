package com.pqixing.intellij.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import com.pqixing.intellij.ui.InstallApkDialog
import com.pqixing.intellij.utils.GradleTaskCallBack
import com.pqixing.intellij.utils.GradleUtils
import jdk.internal.util.xml.impl.Pair
import java.io.File


open class InstallApkAction : AnAction() {

    lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(DataKey.create<Module>("module"))
        val moduleName = module?.name ?: ""
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)?.canonicalPath ?: e.project!!.basePath + "/build/apks"
        val projectMode = /*"ProjectViewPopup".equals(place)||*/"MainMenu" == e.place || module == null || project.name.replace(" ", "") == moduleName || file.endsWith(".apk");

        if (projectMode) {
            val apkDialog = InstallApkDialog(e.project, if (file.endsWith(".apk")) file else findTargetDir(e).absolutePath)
            apkDialog.pack()
            apkDialog.isVisible = true
            return
        }
        val exitCode = Messages.showYesNoCancelDialog("What you want to do?", moduleName, "BuildModule", "JustInstall", "Cancel", null)
        if (exitCode == Messages.CANCEL) return
        if (exitCode == Messages.NO) {
            var dir = findTargetDir(e)
            val apkDialog = InstallApkDialog(e.project, dir.absolutePath)
            apkDialog.pack()
            apkDialog.isVisible = true
            return
        }
        val runTaskId = System.currentTimeMillis().toString()

        val callBack = GradleTaskCallBack { result, log ->
            if (!result) ApplicationManager.getApplication().invokeLater {
                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "BuildApk", "Build $moduleName Apk Fail !!", NotificationType.WARNING).notify(project)
            }
            else ApplicationManager.getApplication().invokeLater {
                val apkDialog = InstallApkDialog(project, log)
                apkDialog.pack()
                apkDialog.isVisible = true
            }
        }
//            AdbShellCommandsUtil.executeCommand()
        GradleUtils.runTask(project, listOf(":$moduleName:PrepareDev", ":$moduleName:BuildApk"), activateToolWindowBeforeRun = true, runTaskId = runTaskId, callback = callBack, envs = mapOf(Pair("include", ""), Pair("dependentModel", "")))
    }

    private fun findTargetDir(e: AnActionEvent): File {
        var dir = File(e.project!!.basePath + "/build/apks")
        if (dir.exists()) {
            val apks = dir.listFiles { file, s -> s.endsWith(".apk") }.sortedBy { it.lastModified() }
            if (apks.isNotEmpty()) dir = apks.last()
        }
        return dir
    }
}
