package com.pqixing.intellij.ui

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.dachen.creator.JekinsJob
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.messages.impl.Message
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.adapter.JListSelectAdapter
import com.pqixing.intellij.adapter.JekinsTrackTask
import com.pqixing.intellij.adapter.JlistSelectListener
import com.pqixing.intellij.utils.UiUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.*
import javax.swing.*

class JekinJobDialog(val project: Project, val curModule: String?, val apps: Map<String, String>, val branchs: List<String>) : BaseJDialog() {
    val apiJson = "api/json"
    val buildJobName = "remoteBuild"
    val jobsUrl = "http://192.168.3.7:8080/jenkins/job/$buildJobName/"
    val queueUrl = "http://192.168.3.7:8080/jenkins/queue/"
    val logStr = "click item to see log"

    var queryTime = 5000L//5毫秒刷新时间
    private lateinit var contentPane: JPanel
    private lateinit var buttonOK: JButton
    private lateinit var jlLog: JLabel
    private lateinit var buttonCancel: JButton
    private lateinit var jlJobs: JList<JListInfo>
    private lateinit var cbModule: JComboBox<String>
    private lateinit var cbBranch: JComboBox<String>
    private lateinit var cbType: JComboBox<String>
    private lateinit var listJobButton: JButton
    private var adapter: JListSelectAdapter

    private var app: String = ""
    private var branch: String = ""
    private var type: String = ""
    val cacheDir: File = File(project.basePath, ".idea/cache/net")

    init {
        setContentPane(contentPane)
        isModal = false
        getRootPane().defaultButton = buttonOK
        title = "Jekins Build"
        buttonOK!!.addActionListener { onOK() }

        buttonCancel!!.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        adapter = JListSelectAdapter(jlJobs, false)
        adapter.selectListener = object : JlistSelectListener {
            override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {
                val job = items.last().data as? JekinsJob ?: return true
                val jobLog = JekinJobLog(project, job.url)
                jobLog.pack()
                jobLog.isVisible = true
                return true
            }

        }
        adapter.setDatas(mutableListOf<JListInfo>().apply {
            for (i in 0..8) this.add(JListInfo(" "))
        })
        val action = ActionListener { Thread(Runnable { onNewJobQuery() }).start() }
        listJobButton.addActionListener(action)
        listJobButton.isVisible = false
        cbModule.addActionListener(action)
        cbBranch.addActionListener(action)
        cbType.addActionListener(action)
        apps.forEach {
            cbModule.addItem(it.key)
            if(it.key== curModule) cbModule.selectedItem = curModule
        }
        branchs.forEach { cbBranch.addItem(it) }
        //开启线程加载
        Thread(Runnable {
            do {
                try {
                    onNewJobQuery()
                    Thread.sleep(queryTime)
                } catch (e: Exception) {
                }
            } while (this@JekinJobDialog.isShowing)
        }).start()
        jlLog.text = logStr
    }

    private fun safeNet(url: String): String = try {
        URL(url).readText()
    } catch (e: Exception) {
        ""
    }

    private fun loadJobById(id: Int): JekinsJob? {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val path = "$id/$apiJson"
        val cacheFile = File(cacheDir, "$buildJobName/$path")
        if (cacheFile.exists()) {
            return JSON.parseObject(cacheFile.readText(), JekinsJob::class.java)
        }

        val json = safeNet("$jobsUrl$path")
        val job = JSON.parseObject(json, JekinsJob::class.java) ?: return null
        if (!job.building) FileUtils.writeText(cacheFile, json)//如果构建已经结束,则缓存到本地
        return job
    }

    private fun loadQueueJob(datas: MutableList<JListInfo>) = try {
        JSON.parseObject(safeNet(queueUrl + apiJson)).getJSONArray("items")?.filter { f1 ->
            f1 is JSONObject && buildJobName == f1.getJSONObject("task")?.getString("name")
                    && checkParam(f1.getString("params")?.split("\n")?.mapNotNull { f2 ->
                val split = f2.trim().split("=")
                if (split.size == 2) Pair(split[0], split[1]) else null
            }?.toMap())
        }?.forEach {
            val o = it as JSONObject
            datas.add(JListInfo("${o.getString("id")}  ${Date(o.getLong("inQueueSince")).toLocaleString()}", "Waiting for executor", 2))
        }
    } catch (e: Exception) {
    }

    private fun getDuration(job: JekinsJob): String {
        var timemills = (if (job.building) (System.currentTimeMillis() - job.timestamp) else job.duration) / 1000
        return "${timemills / 60}分 ${timemills % 60}秒"
    }

    private fun checkParam(toMap: Map<String, String>?): Boolean {
        toMap ?: return false
        return toMap["Apk"] == app && toMap["BranchName"] == branch && toMap["Type"] == type
    }

    /**
     * 查询新的job
     */
    private fun onNewJobQuery(managerOkButton: Boolean = true) {
        app = cbModule.selectedItem.toString()
        branch = cbBranch.selectedItem.toString()
        type = cbType.selectedItem.toString()

        val datas = mutableListOf<JListInfo>()
        //正在队列的id列表
        loadQueueJob(datas)
        var inVisible = datas.isNotEmpty()


        var requestCount = 0
        var id: Int = safeNet(jobsUrl + "lastBuild/buildNumber").toInt()
        while (requestCount++ < 200 && datas.size < 5 && id > 0) {
            val job = loadJobById(id--)
                    ?: continue
            if (!checkParam(job.params)) continue
            inVisible = inVisible or job.building
            datas.add(JListInfo("${job.displayName}   ${Date(job.timestamp).toLocaleString()}", "${getDuration(job)} ${if (job.building) "BUILDING" else job.result}", when (job.result) {
                JekinsJob.SUCCESS -> 1
                JekinsJob.FAILURE, JekinsJob.ABORTED -> 3
                else -> 0
            }).apply { data = job })
            if (requestCount % 50 == 0) adapter.setDatas(datas) //请求50次刷新一下
        }
        if (managerOkButton) buttonOK.isVisible = !inVisible
        adapter.setDatas(datas)
    }


    private fun onOK() {
        val filter = adapter.datas.mapNotNull { it.data }.map { it as JekinsJob }.filter { it.building }
        var msg = "Create new build for $app $branch $type!!"
        if (filter.isNotEmpty()) msg += "\nWarming: ${filter.map { it.displayName }} is building now and will close!!!"
        var exitCode = Messages.showYesNoDialog(project, msg, "Jekins Build", null)
        if (exitCode != Messages.YES) return
        buttonOK.isVisible = false//隐藏Build 按钮
        val lastFirstTitle = adapter.datas.firstOrNull()?.title
        jlLog.text = "Creating Build -> "
        val importTask = object : Task.Backgroundable(project, "Start Build") {
            override fun run(indicator: ProgressIndicator) {
                //停止运行
                filter.forEach { safeNet(jobsUrl + it.number + "/stop?token=remotebyide") }
                //开始运行
                safeNet("${jobsUrl}buildWithParameters?token=remotebyide&Apk=$app&BranchName=$branch&Type=$type&ShowName=${URLEncoder.encode(TextUtils.removeLineAndMark(apps[app]?.replace(" ", "")?:""),"utf-8")
                }")
                var i = 0
                jlLog.text = "Waiting Result"
                while (i++ < 5) {
                    Thread.sleep(queryTime - (i * 1000))
                    jlLog.text = "Querying Result Times -> $i"
                    val first = adapter.datas.firstOrNull() ?: continue
                    indicator.text = "Query Last Item is -> $first.title"
                    if (first.title != lastFirstTitle && first.data != null) break
                }
                onNewJobQuery()
                val first = adapter.datas.firstOrNull()
                ApplicationManager.getApplication().invokeLater {
                    val hasNewBuild = first?.title != lastFirstTitle
                    val data = adapter.datas.firstOrNull()?.data
                    if (hasNewBuild && data == null) {
                        Messages.showMessageDialog("Job is waiting for executor,Please check state later", "Build Result", null)
                    } else if (hasNewBuild && data is JekinsJob && data.building) {//正在构建
                        exitCode = Messages.showYesNoCancelDialog(project, "Create new job ${data.displayName} success , Keep tracking building result in background??", "Build Result", null)
                        if (exitCode == Messages.YES) {
                            val task = JekinsTrackTask(project, "Building Job ${data.displayName} $app $branch $type", data.url + apiJson)
                            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
                            this@JekinJobDialog.onCancel()
                        }
                    } else {
                        Messages.showMessageDialog("Create build fail,Please check reason", "Build Result", null)
                    }
                }
                jlLog.text = logStr
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
