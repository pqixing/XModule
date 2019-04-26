package com.pqixing.intellij.adapter

import com.alibaba.fastjson.JSON
import com.dachen.creator.JekinsJob
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.pqixing.intellij.actions.BuildApkAction
import com.pqixing.intellij.ui.InstallApkDialog
import org.jetbrains.annotations.Nls
import java.net.URL
import javax.swing.event.HyperlinkEvent

class JekinsTrackTask(project: Project?, @Nls(capitalization = Nls.Capitalization.Title) title: String, private val jobUrl: String) : Task.Backgroundable(project, title) {

    override fun run(indicator: ProgressIndicator) {
        var job: JekinsJob? = null
        do {
            Thread.sleep(10000)
            try {
                job = JSON.parseObject(URL(jobUrl).readText(), JekinsJob::class.java)
            } catch (e: Exception) {
            }
        } while (job?.building == true)
        ApplicationManager.getApplication().invokeLater {
            val n = Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, title, "Build Finish -> ${job?.result} -> ${getDuration(job)}  <a href=\"Install\">Install</a> ", if (job?.result == JekinsJob.SUCCESS) NotificationType.INFORMATION else NotificationType.WARNING)
            n.setListener { _, _ ->
                val apkDialog = InstallApkDialog(project, "Select you apk file")
                apkDialog.pack()
                apkDialog.isVisible = true
            }
            n.notify(project)
        }
    }

    private fun getDuration(job: JekinsJob?): String {
        job ?: return ""
        var timemills = (if (job.building) (System.currentTimeMillis() - job.timestamp) else job.duration) / 1000
        return "${timemills / 60}分 ${timemills % 60}秒"
    }
}
