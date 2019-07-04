package com.pqixing.intellij.ui

import android.util.Base64
import com.dachen.creator.utils.AndroidUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.utils.UiUtils

import javax.swing.*
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

class AdbTextDialog(internal var project: Project) : BaseJDialog() {
    private var contentPane: JPanel? = null
    private var toClipButton: JButton? = null
    private var toEditButton: JButton? = null
    private var jText: JTextArea? = null
    private var cbDevices: JComboBox<*>? = null
    private var fromEditButton: JButton? = null
    private var fromClipButton: JButton? = null
    private var refreshButton: JButton? = null

    init {
        setContentPane(contentPane)
        isModal = false
        getRootPane().defaultButton = toClipButton
        title = "Adb Text Editor"
        toClipButton!!.addActionListener { e -> toPhone(false) }
        toEditButton!!.addActionListener { e -> toPhone(true) }
        fromEditButton!!.addActionListener { e -> fromPhone(true) }
        fromClipButton!!.addActionListener { e -> fromPhone(false) }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ e -> onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        AndroidUtils.initDevicesComboBox(project, refreshButton, cbDevices!!)
    }

    private fun toPhone(edit: Boolean) {
        val iDevice = AndroidUtils.getSelectDevice(project, cbDevices!!)
        var result = UiUtils.adbShellCommon(iDevice, "am broadcast -n com.pqixing.clieper/com.pqixing.clieper.ClipHelperReceiver -e text \"check\"", false)
        if (iDevice == null) {
            return
        }
        if (!result.contains("result=success")) {
            val exit = Messages.showOkCancelDialog("Clip helper receiver not found, Install clip helper?", "Miss Application", null)
            if (exit == Messages.OK) installClipHelper()
            return
        }

        val text = String(Base64.encode(jText!!.text.toByteArray(), 0))
        //启动服务
        result = UiUtils.adbShellCommon(iDevice, "am broadcast -n com.pqixing.clieper/com.pqixing.clieper.ClipHelperReceiver -e ${if (edit) "set_text_edit" else "set_text"} \"$text\"", false)
        if (result.contains("result=notpermission")) {//没有权限,弹窗提示,是否打开设置页面
            val exit = Messages.showOkCancelDialog("Miss accessibility service permission , Go to setting ?", "Miss Permission", null)
            if (exit == Messages.OK) UiUtils.adbShellCommon(iDevice, "am start -a android.settings.ACCESSIBILITY_SETTINGS", true)
            return
        } else if (result.contains("result=fail")) Messages.showMessageDialog(if (edit) "set text fail, please check input" else "Unkonw error , please check?", "Error", null)
    }

    private fun installClipHelper() {

    }

    private fun fromPhone(edit: Boolean) {
        val iDevice = AndroidUtils.getSelectDevice(project, cbDevices!!)
        var result = UiUtils.adbShellCommon(iDevice, "am broadcast -n com.pqixing.clieper/com.pqixing.clieper.ClipHelperReceiver -e text \"check\"", false)
        if (iDevice == null) {
            return
        }
        if (!result.contains("result=success")) {
            val exit = Messages.showOkCancelDialog("Clip helper receiver not found, Install clip helper?", "Miss Application", null)
            if (exit == Messages.OK) installClipHelper()
            return
        }
        //启动服务
        result = UiUtils.adbShellCommon(iDevice, "am broadcast -n com.pqixing.clieper/com.pqixing.clieper.ClipHelperReceiver -e ${if (edit) "get_text_edit" else "get_text"} \"get\"", false)
        if (result.contains("result=notpermission")) {//没有权限,弹窗提示,是否打开设置页面
            val exit = Messages.showOkCancelDialog("Miss accessibility service permission , Go to setting ?", "Miss Permission", null)
            if (exit == Messages.OK) UiUtils.adbShellCommon(iDevice, "am start -a android.settings.ACCESSIBILITY_SETTINGS", true)
        } else if (result.contains("result=success")) {
            jText?.text = String(Base64.decode(result.split("result=success")[1].split("\"")[0].trim(), 0))
        } else Messages.showMessageDialog(if (edit) "set text fail, please check input" else "Unkonw error , please check?", "Error", null)
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
