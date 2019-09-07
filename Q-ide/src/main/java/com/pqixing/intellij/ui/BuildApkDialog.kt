package com.pqixing.intellij.ui

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.android.ddmlib.IDevice
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
import com.dachen.creator.JekinsJob
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.concurrency.Semaphore
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.adapter.JListSelectAdapter
import com.pqixing.intellij.adapter.JlistSelectListener
import com.pqixing.intellij.utils.DachenHelper
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.TaskCallBack
import com.pqixing.intellij.utils.UiUtils
import com.pqixing.model.SubModule
import com.pqixing.model.SubModuleType
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import com.pqixing.tools.UrlUtils
import org.bouncycastle.asn1.x500.style.RFC4519Style.c
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
import kotlin.collections.ArrayList

class BuildApkDialog(val project: Project, val configInfo: Any, val activityModel: List<String>, val allModule: Set<SubModule>, val branchs: List<String>) : BaseJDialog() {
    val apiJson = "api/json"
    val buildJobName = "remoteBuild"
    val buildHistory = "buildHistory"
    val jobsUrl = "http://192.168.3.7:8080/jenkins/job/$buildJobName/"
    val queueUrl = "http://192.168.3.7:8080/jenkins/queue/"

    var maxTime = 20//5毫秒刷新时间
    val logAction = ActionListener { sleepTimes = maxTime }
    private lateinit var contentPane: JPanel
    private lateinit var buttonOK: JButton
    private lateinit var btnConfig: JButton
    private lateinit var jspJobs: JScrollPane
    private lateinit var jlJobs: JList<JListInfo>
    private lateinit var jlApps: JList<JListInfo>
    private lateinit var cbBranch: JComboBox<String>
    private lateinit var cbDpModel: JComboBox<String>
    private lateinit var cbType: JComboBox<String>
    private lateinit var cbShowType: JComboBox<String>
    private lateinit var cbAllLog: JCheckBox
    private lateinit var cbBuilder: JCheckBox
    private lateinit var jParams: JTextField
    private var adapter: JListSelectAdapter
    private var appAdapter: JListSelectAdapter

    val format = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    val cacheDir: File = File(project.basePath, ".idea/cache/net")
    val configFile: File = File(project.basePath, ".idea/cache/config/jkconfig")
    var includes: MutableSet<String> = mutableSetOf()
    private val updateCmd = Runnable {
        do {
            if (sleepTimes++ >= maxTime) try {
                sleepTimes = 0
                when (cbShowType.selectedItem) {
                    "Jekins" -> queryJekinsJob()
                    "Local" -> queryLocalBuild()
                }
            } catch (e: Exception) {
            }
            Thread.sleep(300)
        } while (isShowing)
    }

    private fun queryLocalBuild() {
        val allLog: Boolean = cbAllLog.isSelected
        val curBranch = cbBranch.selectedItem.toString()
        val curType = cbType.selectedItem.toString()
        val buildDir = File(cacheDir.parentFile, buildHistory)
        if (!buildDir.exists()) buildDir.mkdirs()
        val selects = appAdapter.datas.filter { it.select }.map { it.title }
        val showLogs = (buildDir.listFiles() ?: emptyArray()).filter { it.isFile && it.name.endsWith(".log") }
                .sortedBy { it.name }.mapNotNull { FileUtils.readText(it) }
                .mapNotNull {
                    val params = UrlUtils.getParams(it)
                    val branch = params["branch"] ?: ""
                    val buildType = params["buildType"] ?: ""
                    val module = params["title"] ?: ""

                    if (!allLog && (curType != buildType || curBranch != branch || (selects.isNotEmpty() && !selects.contains(module)))) return@mapNotNull null

                    val startTime = params["startTime"]?.toLongOrNull() ?: System.currentTimeMillis()
                    val createTime = params["createTime"]?.toLongOrNull() ?: System.currentTimeMillis()
                    val status = params["status"]?.toIntOrNull() ?: 0
                    val buildLog = params["log"] ?: ""
                    val endTime = params["endTime"]?.toLongOrNull() ?: System.currentTimeMillis()
                    val log = when (status) {
                        0 -> "Wait for builder"
                        2 -> "$buildLog : " + (System.currentTimeMillis() - startTime).let { l -> "${l / 1000}秒" }
                        else -> "Spend : " + (endTime - startTime).let { l -> "${l / 1000}秒" }
                    }
                    var title = "${format.format(Date(createTime))}  $module"
                    if (allLog) title += "  $branch  $buildType"
                    JListInfo(title, log, staue = status).apply { data = params["apkUrl"] }
                }
        adapter.setDatas(showLogs)
    }


    init {
        setContentPane(contentPane)
        isModal = false
        getRootPane().defaultButton = buttonOK
        title = "Builder"
        buttonOK.addActionListener { onOK() }

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
        appAdapter = JListSelectAdapter(jlApps, true)


        branchs.forEach { cbBranch.addItem(it) }
        cbShowType.addActionListener { updateShowType() }
        btnConfig.addActionListener { buildConfigClick() }
        cbDpModel.selectedItem = configInfo.javaClass.getField("dependentModel").get(configInfo) ?: "localFirst"
        configInfo.javaClass.getField("include").get(configInfo)?.toString()?.split(",")?.forEach {
            if (it.trim().isNotEmpty()) includes.add(it)
        }
        initUpdateAction()
    }

    var versionPath = ""
    private fun showBuildParams() {
        versionPath = Messages.showInputDialog(project, "Input version path", "Input", null) ?: ""
    }

    override fun showAndPack(): BaseJDialog {
        val t = ArrayList<JListInfo>(350)
        for (i in 0..300) t.add(JListInfo("                                                             "))
        appAdapter.setDatas(t)
        adapter.setDatas(t)
        super.showAndPack()
        updateShowType()
        //开启线程加载
        Thread(updateCmd).start()
        return this;
    }

    fun initUpdateAction() {
        cbBranch.addActionListener(logAction)
        cbType.addActionListener(logAction)
        cbAllLog.addActionListener {
            jspJobs.verticalScrollBarPolicy = if (cbAllLog.isSelected) JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED else JScrollPane.VERTICAL_SCROLLBAR_NEVER
            logAction.actionPerformed(it)
        }
        cbBuilder.addActionListener(logAction)
        appAdapter.selectListener = object : JlistSelectListener {
            override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {
                logAction.actionPerformed(null)
                return false
            }
        }
    }

    val selectListener = object : JlistSelectListener {
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
    var sleepTimes = 0
    private fun updateShowType() {
        val oldDatas = appAdapter.datas.filter { it.select }.map { it.title }
        val newData = when (cbShowType.selectedItem) {
            "Jekins" -> {
                adapter.boxVisible = false
                adapter.selectListener = selectListener
                cbType.isVisible = true
                cbDpModel.isVisible = false
                cbBranch.isVisible = true
                cbBuilder.isVisible = true
                btnConfig.text = "History"
                buttonOK.text = "Build"
                btnConfig.isVisible = true
                jParams.isVisible = false
                allModule.filter { it.type == SubModuleType.TYPE_APPLICATION }.map { JListInfo(it.name, select = activityModel.contains(it.name)) }.sortedBy { !it.select }
            }
            "Local" -> {
                adapter.boxVisible = false
                adapter.selectListener = selectListener
                cbType.isVisible = true
                cbDpModel.isVisible = true
                cbBranch.isVisible = false
                cbBuilder.isVisible = false
                btnConfig.text = "Param"
                buttonOK.text = "Build"
                btnConfig.isVisible = true
                jParams.isVisible = true
                allModule.filter { it.type == SubModuleType.TYPE_APPLICATION || it.child != null }.sortedBy { it.type != SubModuleType.TYPE_APPLICATION }.map { JListInfo(it.name, select = activityModel.indexOf(it.name) == 0) }.sortedBy { !activityModel.contains(it.title) }
            }
            else -> appAdapter.datas
        }
        newData.forEach { if (oldDatas.contains(it.title)) it.select = true }
        appAdapter.setDatas(newData)

        sleepTimes = maxTime
    }

    private fun buildConfigClick() {
        val branch = cbBranch.selectedItem.toString()
        val type = cbType.selectedItem.toString()
        val selectApps = getSelectApps()

        if ("Local" == cbShowType.selectedItem) return showBuildParams()


        val exitCode = Messages.showYesNoCancelDialog("Current Config\nbranch:$branch\nbuildType:$type\nselectApps:${selectApps.joinToString {
            if (selectApps.indexOf(it) % 5 == 0) "\n$it" else ",$it"
        }}", "Select History", "READ", "SAVE", "CANCEL", null)
        if (exitCode == Messages.CANCEL) return
        val save = exitCode == Messages.NO
        val readText = FileUtils.readText(configFile) ?: ""
        val configs = readText.lines().filter { it.trim().isNotEmpty() }.map { it.split("##")[0] to it }.toMutableList()
        val c = Messages.showEditableChooseDialog(if (save) "Input config name for new record" else ""
                , "Select ${if (save) "SAVE" else "READ"} Item", null
                , configs.map { it.first }.toTypedArray()
                , configs.firstOrNull()?.first ?: "", null)?.trim() ?: return
        if (c.isEmpty()) return
        if (save) ApplicationManager.getApplication().runWriteAction {
            configs.removeIf { it.first == c }
            val configStr = "$c##${selectApps.joinToString(",")}##$branch##$type"
            configs.add(0, Pair(name, configStr))
            FileUtils.writeText(configFile, configs.joinToString("\n") { it.second })
        } else configs.find { it.first == c }?.apply {
            val split = second.split("##")
            val modules = split[1].trim().split(",")
            appAdapter.setDatas(appAdapter.datas.onEach { it.select = modules.contains(it.title) }.toList())
            cbBranch.selectedItem = split[2].trim()
            cbType.selectedItem = split[3].trim()

        }
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
        if (!job.building) {//如果构建已经结束,则缓存到本地
            FileUtils.writeText(cacheFile, json)
            //清除记录,只保留200条
            val sortFile = File(cacheDir, buildJobName).listFiles()
            val size = sortFile?.size ?: 0
            if (size > 230) ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    sortFile?.sortedBy { it.name }?.forEachIndexed { index, file -> if (size - index > 200) FileUtils.delete(file) }
                }
            }
        }
        return job
    }

    private fun queryQueueJob(datas: MutableList<JListInfo>) = try {
        val allLog: Boolean = cbAllLog.isSelected
        JSON.parseObject(safeNet(queueUrl + apiJson)).getJSONArray("items")?.filter { f1 ->
            f1 is JSONObject && buildJobName == f1.getJSONObject("task")?.getString("name")
        }?.forEach {
            val o = it as JSONObject
            val p = o.getString("params")?.split("\n")?.mapNotNull { f2 ->
                val split = f2.trim().split("=")
                if (split.size == 2) Pair(split[0], split[1]) else null
            }?.toMap()
            if (checkParam(p)) datas.add(JListInfo("${format.format(Date(o.getLong("inQueueSince")))}  ${p?.get("Apk")
                    ?: o.getString("id")}  ${if (allLog) p?.get("BranchName")
                    ?: "" else ""}  ${if (cbBuilder.isSelected) p?.get("BuildUser")
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
        if (cbAllLog.isSelected) return true
        val branch = cbBranch.selectedItem.toString()
        val type = cbType.selectedItem.toString()
        val apps = getSelectApps()
        return (apps.isEmpty() || apps.contains(toMap["Apk"])) && toMap["BranchName"] == branch && toMap["Type"] == type
    }

    /**
     * 查询新的job
     */
    private fun queryJekinsJob() {
        val datas = mutableListOf<JListInfo>()
        //正在队列的id列表
        queryQueueJob(datas)
        var inVisible = datas.isNotEmpty()
        var requestCount = 0
        var id: Int = safeNet(jobsUrl + "lastBuild/buildNumber").toInt()
        val allLog = cbAllLog.isSelected
        val max = if (cbAllLog.isSelected) Int.MAX_VALUE else 20

        while (requestCount++ < 200 && datas.size < max && id > 0) {
            val job = loadJobById(id--) ?: continue
            if (!checkParam(job.params)) continue
            inVisible = inVisible or job.building
            datas.add(JListInfo("${format.format(Date(job.timestamp))}  ${job.appName
                    ?: job.displayName}  ${if (allLog) job.branch
                    ?: "" else ""}  ${if (cbBuilder.isSelected) job.buildUser
                    ?: "" else ""}", "${getDuration(job)} ${if (job.building) "BUILDING" else job.result}", when (job.result) {
                JekinsJob.SUCCESS -> 1
                JekinsJob.FAILURE, JekinsJob.ABORTED -> 3
                else -> 0
            }).apply { data = job })
        }
        adapter.setDatas(datas)
    }


    private fun onOK() {
        when (cbShowType.selectedItem) {
            "Jekins" -> onJekinBuild()
            "Local" -> onLocalBuild()
        }
    }

    private fun onLocalBuild() {
        val selectItem = appAdapter.datas.filter { it.select }
        if (selectItem.isEmpty()) {
            Messages.showMessageDialog("Please select target apk to install", "Miss Item", null)
            return
        }
        val devices = UiUtils.getDevices(project)
        if (devices.isEmpty()) {
            Messages.showMessageDialog("", "No Deive", null)
            return
        }
        val devicesName = Messages.showEditableChooseDialog("Prepare to install :\n${selectItem.joinToString("\n") { it.title }}", "Install Apk", null, devices.map { it.first }.toTypedArray(), devices.first().first, null)
        val iDevice = devices.findLast { it.first == devicesName }?.second
        if (iDevice == null) {
            Messages.showMessageDialog("Can not find device to run", "Miss Device", null)
            return
        }
        UiUtils.lastDevices = iDevice.serialNumber
        buttonOK.isVisible = false

        val branch = cbBranch.selectedItem.toString()
        val type = cbType.selectedItem.toString()
        val dpModel = cbDpModel.selectedItem.toString()

        val createTime = System.currentTimeMillis()

        var i = 0
        val allMap = selectItem.map { it.title }.map { m ->
            val params = mapOf("branch" to branch, "buildType" to type, "createTime" to (createTime + i++).toString(), "title" to m, "dependentModel" to dpModel)
            params
        }
        writeLocalBuild(allMap)
        startLocalBuild(0, allMap, iDevice)
    }

    private fun writeLocalBuild(params: List<Map<String, String>>) = ApplicationManager.getApplication().invokeAndWait {
        ApplicationManager.getApplication().runWriteAction {
            val buildDir = File(cacheDir.parentFile, buildHistory)
            params.forEach { p ->
                val toUrl = UrlUtils.toUrl("build", "local", p)
                FileUtils.writeText(File(buildDir, "${p["createTime"]}.log"), toUrl)
            }
            sleepTimes = maxTime
        }
    }

    private fun startLocalBuild(index: Int, params: List<Map<String, String>>, iDevice: IDevice) {
        if (index >= params.size) return ApplicationManager.getApplication().invokeAndWait {
            buttonOK.isVisible = true
            isVisible = false
            isVisible = true
        }
        val param = params[index].toMutableMap()
        val title = param["title"] ?: ""
        val dpModel = param["dependentModel"] ?: ""

        param["status"] = "2"
        param["startTime"] = System.currentTimeMillis().toString()
        param["log"] = "Build"
        writeLocalBuild(Collections.singletonList(param))

        GradleUtils.runTask(project, listOf(":$title:PrepareDev", ":$title:clean", ":$title:BuildApk")
                , activateToolWindowBeforeRun = true
                , envs = mapOf("include" to if (dpModel == "mavenOnly") title else "${includes.joinToString(",")},$title", "dependentModel" to dpModel, "versionFile" to versionPath)
                , callback = TaskCallBack { success, result ->

            if (success) {
                val newPath = File(cacheDir.parentFile, "$buildHistory/${param["createTime"]}.apk")
                File(result).renameTo(newPath)
                param["apkUrl"] = newPath.absolutePath
                param["log"] = "Install"
                writeLocalBuild(Collections.singletonList(param))
                val installLog = adbInstall(iDevice, newPath.absolutePath)
                if (installLog.toLowerCase().contains("success")) {
                    val packageId = UiUtils.getAppInfoFromApk(newPath)?.packageId
                    if (packageId != null) {
                        param["log"] = "Open"
                        writeLocalBuild(Collections.singletonList(param))
                        val lauchActivity = AdbShellCommandsUtil.executeCommand(iDevice, "dumpsys package $packageId").output.find { it.contains(packageId) }
                        //打开应用
                        if (lauchActivity?.isNotEmpty() == true) {
                            AdbShellCommandsUtil.executeCommand(iDevice, "am start -n ${lauchActivity.substring(lauchActivity.indexOf(packageId)).split(" ").first().trim()}")
                        }
                    }
                }
            }
            param["endTime"] = System.currentTimeMillis().toString()
            param["status"] = if (success) "1" else "3"
            writeLocalBuild(Collections.singletonList(param))
            startLocalBuild(index + 1, params, iDevice)
        })

    }

    private fun adbInstall(iDevice: IDevice, l: String): String {
        val result = UiUtils.installApk(iDevice, l, jParams.text ?: "")
        //添加本地数据
        val logFiles = File(project.basePath, ".idea/apks.log")
        logFiles.parentFile.mkdirs()
        val list = if (logFiles.exists()) logFiles.readLines().toMutableList() else mutableListOf()
        list.remove(l)
        list.add(0, l)
        while (list.size > 30) list.removeAt(30)
        logFiles.writeText(list.joinToString("\n"))
        return result
    }

    private fun onJekinBuild() {
        val branch = cbBranch.selectedItem.toString()
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

        var msg = "Prepare To Build :\n $runApps \n"
        if (ignoreApps.isNotEmpty()) msg += "\n Ignore (Building or Waiting for executor) :\n $ignoreApps"
        val exitCode = Messages.showYesNoCancelDialog(msg, "Jekins Build", null)
        if (exitCode != Messages.YES) return

        buttonOK.isVisible = false//隐藏Build 按钮
        val importTask = object : Task.Backgroundable(project, "Start Jekins Build") {
            override fun run(indicator: ProgressIndicator) {
                //开始运行
                runApps.forEach { app ->
                    indicator.text = "Start Build $app"
                    safeNet("${jobsUrl}buildWithParameters?token=remotebyide&Apk=$app&BranchName=$branch&Type=$type&ShowName=${URLEncoder.encode(TextUtils.removeLineAndMark(allModule.find { it.name == app }?.introduce?.replace(" ", "")
                            ?: ""), "utf-8")}&BuildUser=${TextUtils.removeLineAndMark(configInfo.javaClass.getField("userName").get(configInfo).toString())}")
                    Thread.sleep(500)//延迟500毫秒再进行请求,避免出现问题
                }
                indicator.text = "Querying Result,Please Wait"
                indicator.text = "Querying Result,Please Wait"
                Thread.sleep(6000)
                buttonOK.isVisible = true//隐藏Build 按钮
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    private fun onCancel() {
        dispose()
    }
}
