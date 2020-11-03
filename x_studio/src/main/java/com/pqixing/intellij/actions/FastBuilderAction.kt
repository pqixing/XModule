package com.pqixing.intellij.actions

import com.android.tools.idea.run.deployment.DeviceGet
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.group.XGroup
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.TaskCallBack
import com.pqixing.intellij.utils.UiUtils
import java.text.SimpleDateFormat
import java.util.*

class FastBuilderAction : AnAction() {

    val fastBuilder = mutableMapOf<Project?, BuildParam?>()

    override fun update(e: AnActionEvent) {
        super.update(e)
        val param = fastBuilder[e.project]
        e.presentation.isVisible = XGroup.isBasic(e.project)&&param!=null
        e.presentation.description = param?.module ?: "FastBuilder"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val param = fastBuilder[e.project] ?: return Messages.showMessageDialog("", "No Found Param", null)
        val device = DeviceGet.getDevice(project)
        if (device == null && Messages.OK != Messages.showOkCancelDialog("No Device Connect,Keep Build ${param.module}?", "Miss Device", null)) {
            return
        }
        val buildCallBack = TaskCallBack { success, result ->
            if (success) {
                param.result = result
                UiUtils.tryInstall(project, device, result, param.install)
            } else ApplicationManager.getApplication().invokeLater {
                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Build Fail", "", NotificationType.WARNING).notify(project)
            }
        }

        GradleUtils.runTask(project, listOf(":${param.module}:BuildApk")
                , envs = mapOf("include" to if (param.module == "mavenOnly") param.module else "${param.module},${XmlHelper.loadConfig(project.basePath!!).include}"
                , "dependentModel" to param.depend, "versionFile" to param.version)
                , callback = buildCallBack)
    }
}

class BuildParam(val str: String = "") {
    var module: String = ""
    var buildType: String = "debug"
    var depend: String = "localFirst"
    var keep: String = "N"
    var install: String = "-r -t"
    var time: String = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss",Locale.CHINA).format(Date())
    var version: String = ""
    var result: String = ""

    init {
        if (str.isNotEmpty()) {
            val split = str.split(",")
            module = split[0]
            buildType = split[1]
            depend = split[2]
            keep = split[3]
            time = split[4]
            version = split[5]
            result = split[6]
            install = split[7]
        }
    }

    override fun toString(): String {
        return "$module,$buildType,$depend,$keep,$time,$version,$result,$install"
    }
}