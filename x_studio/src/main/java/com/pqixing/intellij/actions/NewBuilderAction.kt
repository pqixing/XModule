package com.pqixing.intellij.actions

import com.android.tools.idea.run.deployment.DeviceGet
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.ui.NewBuilderDialog
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.TaskCallBack
import com.pqixing.intellij.utils.UiUtils
import com.pqixing.tools.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewBuilderAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val all = XmlHelper.loadAllModule(project.basePath).filter { it.isAndroid&&!it.attach() }.map { it.name }
        val modules = ModuleManager.getInstance(project).sortedModules.filter { all.contains(it.name) }.mapNotNull { it.name }.let { it.plus(all.filter { f -> !it.contains(f) }) }
        val curModule = e.getData(LangDataKeys.MODULE)?.name?.takeIf { modules.contains(it) } ?: modules.firstOrNull()
        val config = XmlHelper.loadConfig(project.basePath!!)

        val buildParam = BuildParam()
        buildParam.module = curModule ?: ""
        buildParam.depend = config.dependentModel

        val logFile = File(project.basePath, UiUtils.BUILD_PROPERTIES)
        val logs = (FileUtils.readText(logFile)
                ?: "").lines().filter { it.isNotEmpty() }.map { BuildParam(it) }.toMutableList()
        val dialog = NewBuilderDialog(project, buildParam, modules, logs)
        dialog.btnVersion.addActionListener {
            FileChooser.chooseFiles(FileChooserDescriptor(true, false, false, false, false, false)
                    , project, project.projectFile) { files: List<VirtualFile> ->
                files.firstOrNull()?.let { dialog.tvVersion.text = it.canonicalPath }
                dialog.resetBtn(false)
            }
        }

        dialog.setOnOk {
            val param = dialog.param
            val device = DeviceGet.getDevice(project)
            if (device == null && Messages.OK != Messages.showOkCancelDialog("No Device Connect,Keep Build ${param.module}?", "Miss Device", null)) {
                return@setOnOk
            }

            val fastBuilder = (ActionManager.getInstance().getAction("XModule.fastBuilder") as? FastBuilderAction)?.fastBuilder
            if ("Y" == param.keep) fastBuilder?.put(project, param) else fastBuilder?.remove(project)

            logs.add(0, param)
            param.time = "building..."
            dialog.refreshHistory()
            val buildCallBack = TaskCallBack { success, result ->
                if (success) {
                    param.result = result
                    UiUtils.tryInstall(project, device, result, param.install)
                }else ApplicationManager.getApplication().invokeLater {
                    Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Build Fail", "", NotificationType.WARNING).notify(project)
                }
                param.time = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss",Locale.CHINA).format(Date())
                dialog.refreshHistory()
                while (logs.size > 50) logs.removeAt(logs.size - 1)
                FileUtils.writeText(logFile, logs.joinToString("\n"))
            }
            GradleUtils.runTask(project, listOf(":${param.module}:BuildApk")
                    , envs = mapOf("include" to if (param.module == "mavenOnly") param.module else "${param.module},${config.include}"
                    , "dependentModel" to param.depend, "versionFile" to param.version)
                    , callback = buildCallBack)
        }

        dialog.btnInstall.addActionListener { UiUtils.tryInstall(project, null, dialog.param.result, dialog.param.install) }
        dialog.showAndPack()

    }



}