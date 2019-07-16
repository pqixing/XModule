package com.pqixing.intellij.ui

import android.util.Base64
import com.android.ddmlib.IDevice
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.utils.DachenHelper
import com.pqixing.intellij.utils.UiUtils
import java.awt.Desktop
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
import javax.swing.*

class AdbTextDialog(var project: Project) : BaseJDialog() {
    private var contentPane: JPanel? = null
    private var toClipButton: JButton? = null
    private var toEditButton: JButton? = null
    public var jText: JTextArea? = null
    private var cbDevices: JComboBox<String>? = null
    private var fromEditButton: JButton? = null
    private var fromClipButton: JButton? = null
    private var clipVersion = "1.1"
    val resultKey = "onIdeResult="

    init {
        setContentPane(contentPane)
        isModal = false
        title = "Adb Text Editor"
        toClipButton!!.addActionListener { e -> toPhone(false,toClipButton) }
        toEditButton!!.addActionListener { e -> toPhone(true,toEditButton) }
        fromEditButton!!.addActionListener { e -> fromPhone(true,fromEditButton) }
        fromClipButton!!.addActionListener { e -> fromPhone(false,fromClipButton) }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dispose()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ e -> dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        UiUtils.initDevicesComboBox(project, cbDevices!!)
        UiUtils.setTransfer(jText!!) {
            jText!!.text = it.joinToString("\n")
        }
    }

    /**
     * 执行命令并且返回结果
     */
    private fun runAdCommand(device: IDevice, cmd: String): String? {
        val result = AdbShellCommandsUtil.executeCommand(device, cmd).output.toString()
        if(!result.contains(resultKey)) return null

        val start = result.indexOf(resultKey) + resultKey.length
        val end = result.indexOf("\"", start)
        return String(Base64.decode(result.substring(start, if (end == -1) result.length else end).trim(), 0))
    }


    private fun getBroadCastCmd(key: String, value: String? = null): String {
        return "am broadcast -a com.peqixing.clip.helper -e $key \"${String(Base64.encode((value
                ?: key).toByteArray(), 0))}\""
    }

    private fun checkHelper(iDevice: IDevice): Boolean {
        var result = runAdCommand(iDevice, getBroadCastCmd("get_version"))?:kotlin.run {
            //获取失败
            //尝试启动应用,然后重新获取
            runAdCommand(iDevice, "am start -n com.pqixing.clieper/com.pqixing.clieper.MainActivity")
            runAdCommand(iDevice, getBroadCastCmd("get_version"))
        }
        val unVail = result == null || result < clipVersion
        if (unVail) ApplicationManager.getApplication().invokeLater {
            val exit = Messages.showOkCancelDialog("请手动启动PC输入助手并,如未安装,是否下载安装?", "启动助手应用失败", null)
            if (exit == Messages.OK) installClipHelper(iDevice)
        }
        return !unVail
    }

    private fun toPhone(edit: Boolean, btn: JButton?) = ProgressManager.getInstance().runProcess({
        val iDevice = UiUtils.getSelectDevice(project, cbDevices!!) ?: return@runProcess
        btn?.isEnabled = false
        if (!checkHelper(iDevice)){
            btn?.isEnabled = true
            return@runProcess
        }

        val adCommand = runAdCommand(iDevice, getBroadCastCmd(if (edit) "set_text_edit" else "set_text", jText!!.text))
        if ("##permission##" == adCommand) ApplicationManager.getApplication().invokeLater {
            val exit = Messages.showOkCancelDialog("PC输入助手 没有获取辅助权限,请去设置中打开打开或者关闭再打开辅助权限?", "权限", null)
            if (exit == Messages.OK) runAdCommand(iDevice, "am start -a android.settings.ACCESSIBILITY_SETTINGS")
        } else if ("##fail##" == adCommand) ApplicationManager.getApplication().invokeLater {
            Messages.showMessageDialog(if (edit) "设置文本失败,请检查当前页面焦点" else "无法设置文本,请检查", "未知错误", null)
        }
        btn?.isEnabled = true
    }, null)

    private fun fromPhone(edit: Boolean, btn: JButton?) = ProgressManager.getInstance().runProcess({
        val iDevice = UiUtils.getSelectDevice(project, cbDevices!!) ?: return@runProcess
        btn?.isEnabled = false
        if (!checkHelper(iDevice)){
            btn?.isEnabled = true
            return@runProcess
        }

        var adCommand = runAdCommand(iDevice, getBroadCastCmd(if (edit) "get_text_edit" else "get_text"))
        if ("##permission##" == adCommand) ApplicationManager.getApplication().invokeLater {
           val exit =  Messages.showOkCancelDialog("PC输入助手 没有获取辅助权限,请去设置中打开打开辅助权限?", "权限", null)
            if (exit == Messages.OK) runAdCommand(iDevice, "am start -a android.settings.ACCESSIBILITY_SETTINGS")
        } else if ("##fail##" == adCommand || adCommand == null) ApplicationManager.getApplication().invokeLater {
            Messages.showMessageDialog(if (edit) "设置文本失败,请检查当前页面焦点" else "无法设置文本,请检查", "未知错误", null)
        } else jText?.text = adCommand
        btn?.isEnabled = true
    }, null)

    private fun installClipHelper(iDevice: IDevice) {
        val install = object : Task.Backgroundable(project, "Start Install") {

            override fun run(indicator: ProgressIndicator) {
                val url = "https://raw.githubusercontent.com/pqixing/modularization/master/jars/clip-helper.apk"
                indicator.text = "Download : $url"
                try {
                    val downloadApk = DachenHelper.downloadApk(project, "copy", url)
                    if (downloadApk.isEmpty() || !File(downloadApk).exists()) ApplicationManager.getApplication().invokeLater {
                        if (Messages.OK == Messages.showOkCancelDialog("请尝试使用浏览器进行下载并手动安装?", "下载失败", null)) {
                            Desktop.getDesktop().browse(URI(url))
                        }
                    } else {
                        indicator.text = "Install : $url"
                        UiUtils.installApk(iDevice, downloadApk, "-r -t")
                        UiUtils.adbShellCommon(iDevice, "am start -n com.pqixing.clieper/com.pqixing.clieper.MainActivity", false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(install, BackgroundableProcessIndicator(install))

    }

}
