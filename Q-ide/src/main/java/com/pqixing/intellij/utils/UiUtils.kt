package com.pqixing.intellij.utils

import com.android.ddmlib.IDevice
import com.android.tools.apk.analyzer.AaptInvoker
import com.android.tools.apk.analyzer.AndroidApplicationInfo
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
import com.android.tools.idea.sdk.AndroidSdks
import com.dachen.creator.utils.LogWrap
import com.intellij.openapi.project.Project
import org.jetbrains.android.sdk.AndroidSdkUtils

import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.event.ActionEvent
import java.io.File
import java.util.ArrayList
import javax.swing.*

object UiUtils {
    val IDE_PROPERTIES = ".idea/modularization.properties"
    var lastDevices = ""

    fun setTransfer(component: JComponent, block: (files: List<File>) -> Unit) {
        component.transferHandler = object : TransferHandler() {
            override fun importData(p0: JComponent?, t: Transferable): Boolean {
                try {
                    val o = t.getTransferData(DataFlavor.javaFileListFlavor)

                    block(o as List<File>)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }

            override fun canImport(p0: JComponent?, flavors: Array<out DataFlavor>?): Boolean {
                return flavors?.find { DataFlavor.javaFileListFlavor.equals(it) } != null
            }
        }
    }

    fun getSelectDevice(project: Project, comboBox: JComboBox<*>): IDevice? {
        val id = comboBox.selectedItem?.toString() ?: ""
        return getDevices(project).find { it.first == id }?.second
    }

    fun initDevicesComboBox(project: Project, refreshButton: JButton?, comboBox: JComboBox<String>) {
        val a = { e: ActionEvent? ->
            comboBox.removeAllItems()
            getDevices(project).forEach { i -> comboBox.addItem(i.first) }
        }
        a(null)
        refreshButton?.addActionListener(a)
    }

    fun getDevices(project: Project): List<Pair<String, IDevice>> {
        val infos = ArrayList<Pair<String, IDevice>>()
        AndroidSdkUtils.getDebugBridge(project)?.devices?.forEach { d ->
            var avdName: String = d.avdName
                    ?: (UiUtils.adbShellCommon(d, "getprop ro.product.brand", true) + "-" + UiUtils.adbShellCommon(d, "getprop ro.product.model", true))
            val p = Pair(avdName, d)
            if (lastDevices == d.serialNumber)
                infos.add(0, p)
            else
                infos.add(p)
        }
        return infos
    }

    fun installApk(device: IDevice, path: String, params: String): String = try {
        val newPath = "/data/local/tmp/${path.hashCode()}.apk"
        device.pushFile(path, newPath)
        val r = AdbShellCommandsUtil.executeCommand(device, "pm  install $params  $newPath").output.findLast { it.trim().isNotEmpty() }
                ?: "Unknow Exception"
        if (r.contains("Success")) adbShellCommon(device, "rm -f $newPath", false)
        else adbShellCommon(device, "mv $newPath /sdcard/${path.substringAfterLast("/")}", false)
        r
    } catch (e: Exception) {
        e.toString()
    }

    fun adbShellCommon(device: IDevice, cmd: String, firstLine: Boolean): String = try {
        val output = AdbShellCommandsUtil.executeCommand(device, cmd).output
        if (firstLine || output.size == 1) output[0] else output.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    fun getAppInfoFromApk(fileApk: File): AndroidApplicationInfo? = try {
        val invoker = AaptInvoker(AndroidSdks.getInstance().tryToChooseSdkHandler(), LogWrap())
        val xmlTree = invoker.getXmlTree(fileApk, "AndroidManifest.xml")
        AndroidApplicationInfo.parse(xmlTree)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
