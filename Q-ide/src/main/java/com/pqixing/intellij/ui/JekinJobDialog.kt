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
import java.awt.Desktop
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

class JekinJobDialog(val project: Project, val userName: String?, val curModule: String?, val apps: Map<String, String>, val branchs: List<String>) : BaseJDialog() {
    val apiJson = "api/json"
    val buildJobName = "remoteBuild"
    val jobsUrl = "http://192.168.3.7:8080/jenkins/job/$buildJobName/"
    val queueUrl = "http://192.168.3.7:8080/jenkins/queue/"
    val logStr = "click item to see log"

    var queryTime = 5000L//5毫秒刷新时间
    private lateinit var contentPane: JPanel
    private lateinit var buttonOK: JButton
    private lateinit var jspJobs: JScrollPane
    private lateinit var jlLog: JLabel
    private lateinit var jlJobs: JList<JListInfo>
    private lateinit var jlApps: JList<JListInfo>
    private lateinit var cbBranch: JComboBox<String>
    private lateinit var cbType: JComboBox<String>
    private lateinit var cbAllLog: JCheckBox
    private var adapter: JListSelectAdapter
    private var appAdapter: JListSelectAdapter

    val format = SimpleDateFormat("MM-dd HH:mm")

    val cacheDir: File = File(project.basePath, ".idea/cache/net")

    init {
        setContentPane(contentPane)
        isModal = false
        getRootPane().defaultButton = buttonOK
        title = "Jekins Build"
        buttonOK!!.addActionListener { onOK() }

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
                if (job.displayName == null) {
                    Desktop.getDesktop().browse(URI(jobsUrl))
                } else {
                    val jobLog = JekinJobLog(project, job.url)
                    jobLog.pack()
                    jobLog.isVisible = true
                }
                return true
            }
        }

        adapter.setDatas(mutableListOf<JListInfo>().apply {
            for (i in 0..200) this.add(JListInfo(" "))
        })
        val action = ActionListener {
            jspJobs.verticalScrollBarPolicy = if (cbAllLog.isSelected) JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED else JScrollPane.VERTICAL_SCROLLBAR_NEVER
            Thread { onNewJobQuery() }.start()
        }
        cbBranch.addActionListener(action)
        cbType.addActionListener(action)
        cbAllLog.addActionListener(action)
        appAdapter = JListSelectAdapter(jlApps, true)
        appAdapter.setDatas(apps.map { JListInfo(it.key, select = it.key == curModule) })
        appAdapter.selectListener = object : JlistSelectListener {
            override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {
                action.actionPerformed(null)
                return false
            }
        }

        cbBranch.addItem(branchs[0])
        cbBranch.addItem("")
        for (i in 1 until branchs.size) cbBranch.addItem(branchs[i])

        //开启线程加载
        Thread {
            while (true) try {
                onNewJobQuery()
                Thread.sleep(queryTime)
                if (!this@JekinJobDialog.isShowing) break
            } catch (e: Exception) {
            }
        }.start()
        resetLog()
    }

    private fun resetLog() {
        val selectApps = getSelectApps()
        jlLog.text = if (selectApps.isEmpty()) logStr else selectApps.toString()
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
        val branch = cbBranch.selectedItem.toString()
        JSON.parseObject(safeNet(queueUrl + apiJson)).getJSONArray("items")?.filter { f1 ->
            f1 is JSONObject && buildJobName == f1.getJSONObject("task")?.getString("name")
        }?.forEach {
            val o = it as JSONObject
            val p = o.getString("params")?.split("\n")?.mapNotNull { f2 ->
                val split = f2.trim().split("=")
                if (split.size == 2) Pair(split[0], split[1]) else null
            }?.toMap()
            if (checkParam(p)) datas.add(JListInfo("${format.format(Date(o.getLong("inQueueSince")))}  ${p?.get("Apk")
                    ?: o.getString("id")}  ${if (branch.isEmpty()) p?.get("BranchName")
                    ?: "" else ""}", "Waiting for executor", 2).apply {
                data = JekinsJob().apply { params = p!! }
            })
        }
    } catch (e: Exception) {
    }

    private fun getDuration(job: JekinsJob): String {
        var timemills = (if (job.building) (System.currentTimeMillis() - job.timestamp) else job.duration) / 1000
        return "${timemills / 60}分 ${timemills % 60}秒"
    }

    private fun getSelectApps() = appAdapter.datas.filter { it.select }.map { it.title }

    private fun checkParam(toMap: Map<String, String>?): Boolean {
        toMap ?: return false
        val branch = cbBranch.selectedItem.toString()
        val type = cbType.selectedItem.toString()
        val apps = getSelectApps()
        return (apps.isEmpty() || apps.contains(toMap["Apk"])) && (branch.isEmpty() || toMap["BranchName"] == branch) && (type.isEmpty() || toMap["Type"] == type)
    }

    /**
     * 查询新的job
     */
    private fun onNewJobQuery() {
        val datas = mutableListOf<JListInfo>()
        //正在队列的id列表
        loadQueueJob(datas)
        var inVisible = datas.isNotEmpty()
        var requestCount = 0
        var id: Int = safeNet(jobsUrl + "lastBuild/buildNumber").toInt()
        val branch = cbBranch.selectedItem.toString()
        val max = if (cbAllLog.isSelected) Int.MAX_VALUE else 20

        while (requestCount++ < 200 && datas.size < max && id > 0) {
            val job = loadJobById(id--)
                    ?: continue
            if (!checkParam(job.params)) continue
            inVisible = inVisible or job.building
            datas.add(JListInfo("${format.format(Date(job.timestamp))}  ${job.appName
                    ?: job.displayName}  ${if (branch.isEmpty()) job.branch
                    ?: "" else ""}", "${getDuration(job)} ${if (job.building) "BUILDING" else job.result}", when (job.result) {
                JekinsJob.SUCCESS -> 1
                JekinsJob.FAILURE, JekinsJob.ABORTED -> 3
                else -> 0
            }).apply { data = job })
        }
        adapter.setDatas(datas)
    }


    private fun onOK() {
        val branch = cbBranch.selectedItem.toString()
        if (branch.isEmpty()) {
            Messages.showMessageDialog("Branch is not allow empty!!", "Warning", null)
            return
        }
        val type = cbType.selectedItem.toString()
        val selectApps = getSelectApps()
        if (selectApps.isEmpty()) {
            Messages.showMessageDialog("BuildApp is not allow empty!!", "Warning", null)
            return
        }

        //当前正在构建的App
        val buildingApp = adapter.datas.mapNotNull { it.data }.map { it as JekinsJob }.filter { it.building && branch == it.branch && type == it.type }.map { it.appName }

        val runApps = selectApps.toMutableList().apply { removeAll(buildingApp) }
        val ignoreApps = selectApps.toMutableList().apply { removeAll(runApps) }

        val exitCode = Messages.showYesNoCancelDialog("Prepare :\n $runApps \n\n Ignore (Building or Waiting for executor) :\n$ignoreApps", "Jekins Build", null)
        if (exitCode != Messages.YES) return

        buttonOK.isVisible = false//隐藏Build 按钮
        val importTask = object : Task.Backgroundable(project, "Start Jekins Build") {
            override fun run(indicator: ProgressIndicator) {
                queryTime = 1000L
                //开始运行
                runApps.forEach { app ->
                    indicator.text = "Start Build $app"
                    jlLog.text = "Start Build $app"
                    safeNet("${jobsUrl}buildWithParameters?token=remotebyide&Apk=$app&BranchName=$branch&Type=$type&ShowName=${URLEncoder.encode(TextUtils.removeLineAndMark(apps[app]?.replace(" ", "")
                            ?: ""), "utf-8")}BuildUser=${userName}")
                    Thread.sleep(500)//延迟500毫秒再进行请求,避免出现问题
                }
                indicator.text = "Querying Result,Please Wait"
                indicator.text = "Querying Result,Please Wait"
                Thread.sleep(6000)
                queryTime = 5000L
                buttonOK.isVisible = true//隐藏Build 按钮
                resetLog()
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    private fun onCancel() {
        dispose()
    }
}
