package com.pqixing.intellij.ui

import com.alibaba.fastjson.JSON
import com.dachen.creator.JekinsJob
import com.intellij.openapi.project.Project
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.util.*
import javax.swing.*

class JekinJobLog(val project: Project, val jobUrl: String) : BaseJDialog() {
    private lateinit var contentPane: JPanel
    private lateinit var tpLog: JTextPane
    private lateinit var stopButton: JButton
    private lateinit var jlTitle: JLabel
    private lateinit var jpScroll: JScrollPane
    private lateinit var autoRefresh: JCheckBox

    init {
        setContentPane(contentPane)
        isModal = false
        title = "Console"
        stopButton.addActionListener { Desktop.getDesktop().browse(URI(jobUrl)) }
        autoRefresh.addActionListener { reloadJob() }
        reloadJob()
    }

    fun reloadJob() = Thread(Runnable {
        var job: JekinsJob? = null
        do {
            try {
                job = JSON.parseObject(URL(jobUrl + "api/json").readText(), JekinsJob::class.java)
                jlTitle.text = "${job.displayName} -> ${Date(job.timestamp).toLocaleString()} -> ${getDuration(job)} -> ${if (job.building) "BUILDING" else job.result}"
                tpLog.text = URL(jobUrl + "consoleText").readText().lines().reversed().joinToString(separator = "\n")
            } catch (e: Exception) {
            }
            Thread.sleep(3000)
        } while (job?.building == true && autoRefresh.isSelected)
    }).start()

    private fun getDuration(job: JekinsJob?): String {
        job ?: return ""
        var timemills = (if (job.building) (System.currentTimeMillis() - job.timestamp) else job.duration) / 1000
        return "${timemills / 60}分 ${timemills % 60}秒"
    }
}
