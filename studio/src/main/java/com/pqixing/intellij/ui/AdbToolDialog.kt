//package com.pqixing.intellij.ui
//
//import com.android.ddmlib.IDevice
//import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
//import com.intellij.openapi.progress.ProgressIndicator
//import com.intellij.openapi.progress.ProgressManager
//import com.intellij.openapi.progress.Task
//import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.ui.Messages
//import com.intellij.util.concurrency.Semaphore
//import com.pqixing.intellij.group.QToolGroup
//import com.pqixing.intellij.adapter.JListInfo
//import com.pqixing.intellij.adapter.JListSelectAdapter
//import com.pqixing.intellij.utils.DachenHelper
//import com.pqixing.intellij.utils.IInstall
//import com.pqixing.intellij.utils.UiUtils
//import java.awt.event.KeyEvent
//import java.awt.event.WindowAdapter
//import java.awt.event.WindowEvent
//import java.io.File
//import java.util.*
//import javax.swing.*
//
//class AdbToolDialog(val project: Project, apkPath: String?) : BaseJDialog(project) {
//    private lateinit var contentPane: JPanel
//    private lateinit var buttonOK: JButton
//    private lateinit var jParams: JTextField
//    private lateinit var localRadioButton: JRadioButton
//    private lateinit var netRadioButton: JRadioButton
//    private lateinit var devices: JComboBox<String>
//    private lateinit var jlDatas: JList<JListInfo>
//    private lateinit var openAppCheckBox: JCheckBox
//    private lateinit var cbEdit: JCheckBox
//    private lateinit var taText: JTextArea
//    private lateinit var btnFrom: JButton
//    private lateinit var btnTo: JButton
//    private  var adapter: JListSelectAdapter
//    private  val apkPaths = arrayListOf<JListInfo>()
//    private  val netApks = arrayListOf<JListInfo>()
//    private  var model = 0;
//    private var radios = arrayListOf<Pair<JRadioButton, List<JListInfo>>>()
//
//    init {
//        setContentPane(contentPane)
//        isModal = false
//        title = "Adb Tools"
//        getRootPane().defaultButton = buttonOK
//
//        buttonOK!!.addActionListener { onOK() }
//
//        // call onCancel() when cross is clicked
//        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
//        addWindowListener(object : WindowAdapter() {
//            override fun windowClosing(e: WindowEvent?) {
//                onCancel()
//            }
//        })
//        radios.add(Pair(localRadioButton!!, apkPaths))
//        radios.add(Pair(netRadioButton!!, netApks))
//        for (i in 0 until radios.size) radios[i].first.addActionListener { model = i;updateUI() }
//
//        // call onCancel() on ESCAPE
//        contentPane!!.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
//        adapter = JListSelectAdapter(jlDatas!!, true)
//        var ss = arrayListOf<JListInfo>()
//        for (i in 0..100) ss.add(JListInfo(""))
//        adapter.setDatas(ss)
//        UiUtils.addDevicesComboBox(project, devices!!)
//        UiUtils.setTransfer(jlDatas) { f ->
//            model = 0
//            addApkPaths(f.filter { checkIsApk(it.name) }, true)
//            updateUI()
//        }
//        if (!QToolGroup.isDachenProject(project)) netRadioButton.isVisible = false
//        model = if (checkIsApk(apkPath)) 0 else 1
//        initData(apkPath)
//        AdbTextDialog(project,devices,taText,btnFrom,btnTo,cbEdit).init()
//    }
//
//    override fun dispose() {
//        UiUtils.removeDevicesComboBox(devices)
//        super.dispose()
//    }
//    fun checkIsApk(url: String?) = url?.endsWith(".apk") ?: false
//
//    private fun addApkPaths(files: List<File>, select: Boolean = false): List<JListInfo> {
//        val js = files.map { f ->
//            apkPaths.find { it.infoId == f.absolutePath }?.apply { this.select = select }
//                    ?: JListInfo(f.name + " : .." + f.absolutePath.substring(Math.max(0, f.absolutePath.length - 30)), "", 0, select)
//                            .apply {
//                                infoId = f.absolutePath
//                                data = IInstall { _, _,_, _, c -> c.onTaskEnd(f.exists(), f.absolutePath) }
//                            }
//        }
//        apkPaths.removeAll(js)
//        apkPaths.addAll(0, js)
//        return js
//    }
//
//    private fun updateUI() {
//        //如果当前模式不可见,切换会默认模式
//        if (!radios[model].first.isVisible) {
//            model = 0
//            updateUI()
//            return
//        }
//        for (i in 0 until radios.size) radios[i].first.isSelected = model == i
//        adapter.setDatas(radios[model].second)
//    }
//
//    private fun initData(apkPath: String?) = Thread {
//        val start = System.currentTimeMillis();
//        //添加本地数据
//        val logFiles = File(project.basePath, ".idea/apks.log")
//        if (logFiles.exists()) {
//            addApkPaths(logFiles.readLines().map { File(it) })
//        }
//        if (checkIsApk(apkPath)) addApkPaths(Collections.singletonList(File(apkPath!!)), true)
//        //添加网络数据
//        if (netRadioButton!!.isVisible) DachenHelper.loadApksForNet().forEach {
//            val j = JListInfo(it.key).apply {
//                data = IInstall { _, _,_, i, c ->
//                    i.text = "Download : ${it.value}"
//                    val downloadApk = DachenHelper.downloadApk(project, it.key.split(" ").last().trim(), it.value);
//                    c.onTaskEnd(downloadApk.isNotEmpty(), downloadApk)
//                }
//            }
//            netApks.add(j)
//        }
//        val after = 100 - System.currentTimeMillis() + start
//        if (after > 0) Thread.sleep(after)
//        updateUI()
//    }.start()
//
//    private fun onOK() {
//        val selectItem = adapter.datas.filter { it.select }
//        if (selectItem.isEmpty()) {
//            Messages.showMessageDialog("Please select target apk to install", "Miss Item", null)
//            return
//        }
//        val iDevice = UiUtils.getSelectDevice( devices!!)
//        if (iDevice == null) {
//            Messages.showMessageDialog("Can not find device to run", "Miss Device", null)
//            return
//        }
//        buttonOK.isVisible = false
//        val install = object : Task.Backgroundable(project, "Start Install") {
//            override fun run(indicator: ProgressIndicator) {
//                val targetDone = Semaphore()
//                targetDone.down()
//                installApp(0, selectItem, indicator, targetDone, openAppCheckBox.isSelected, iDevice)
//                targetDone.waitFor()
//                val failItem = selectItem.filter { it.staue == 3 }
//                if (failItem.isNotEmpty()) {
//                    model = 0
//                    updateUI()
//                }
//                buttonOK.isVisible = true
//            }
//        }
//        ProgressManager.getInstance().runProcessWithProgressAsynchronously(install, BackgroundableProcessIndicator(install))
//    }
//
//    private fun installApp(i: Int, selectItem: List<JListInfo>, indicator: ProgressIndicator, targetDone: Semaphore, openApk: Boolean, iDevice: IDevice) {
//        if (i >= selectItem.size) {// 执行完毕
//            indicator.text = "Install Finish"
//            targetDone.up()
//            buttonOK!!.isVisible = true
//            return
//        }
//        val info = selectItem[i]
//        info.staue = 2
//        info.log = "-----"
//        updateUI()
//        (info.data as IInstall).onInstall(info, i,this, indicator) { s, l ->
//            if (!s) {
//                info.staue = 3
//                info.log = l
//                updateUI()
//                installApp(i + 1, selectItem, indicator, targetDone, openApk, iDevice)
//                return@onInstall
//            }
//            indicator.text = "Install -> $l"
//            val lastLog = adbInstall(iDevice, l)
//            val hadInstall = lastLog.toLowerCase().contains("success")
//            info.log = lastLog
//            info.staue = if (hadInstall) 1 else 3
//            if (openApk && hadInstall) {
//                indicator.text = "Try Open App -> $l"
//                val packageId = UiUtils.getAppInfoFromApk(File(l))?.packageId
//                if (packageId != null) {
//
//                    val lauchActivity = AdbShellCommandsUtil.executeCommand(iDevice, "dumpsys package $packageId").output.find { it.contains(packageId) }
//                    //打开应用
//                    if (lauchActivity?.isNotEmpty() == true) {
//                        AdbShellCommandsUtil.executeCommand(iDevice, "am start -n ${lauchActivity.substring(lauchActivity.indexOf(packageId)).split(" ").first().trim()}")
//                    }
//                }
//            }
//            updateUI()
//            installApp(i + 1, selectItem, indicator, targetDone, openApk, iDevice)
//        }
//    }
//
//    private fun adbInstall(iDevice: IDevice, l: String): String {
//        val result = UiUtils.installApk(iDevice, l, jParams?.text ?: "")
//        val success = result.toLowerCase().contains("Success")
//        addApkPaths(Collections.singletonList(File(l)), result != "success")[0].apply {
//            staue = if (success) 1 else 3
//            log = result
//        }
//        //添加本地数据
//        val logFiles = File(project.basePath, ".idea/apks.log")
//        logFiles.parentFile.mkdirs()
//        val list = if (logFiles.exists()) logFiles.readLines().toMutableList() else mutableListOf()
//        list.remove(l)
//        list.add(0, l)
//        while (list.size > 30) list.removeAt(30)
//        logFiles.writeText(list.joinToString("\n"))
//        return result
//    }
//
//    private fun onCancel() {
//        // add your code here if necessary
//        dispose()
//    }
//}
