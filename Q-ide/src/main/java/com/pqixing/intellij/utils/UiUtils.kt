package com.pqixing.intellij.utils

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.tools.apk.analyzer.AaptInvoker
import com.android.tools.apk.analyzer.AndroidApplicationInfo
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
import com.android.tools.idea.sdk.AndroidSdks
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.*
import com.pqixing.creator.utils.LogWrap
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.ui.NewImportDialog
import com.pqixing.model.ProjectXmlModel
import com.pqixing.tools.PropertiesUtils.readProperties
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.util.*
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.TransferHandler

object UiUtils : AndroidDebugBridge.IDeviceChangeListener, VirtualFileListener {
    override fun deviceConnected(p0: IDevice) {
        if (devices.find { p0.serialNumber == it.second.serialNumber } == null) {
            val newItem = Pair(p0.getDevicesName(), p0)
            devices.add(newItem)
            comboxs.forEach { it.addItem(newItem.first) }
        }
    }

    override fun deviceDisconnected(p0: IDevice) {
        val item = devices.find { p0.serialNumber == it.second.serialNumber } ?: return
        devices.remove(item)
        comboxs.forEach { it.removeItem(item.first) }
    }

    override fun deviceChanged(p0: IDevice?, p1: Int) {

    }

    val IDE_PROPERTIES = ".idea/caches/import.properties"
    val IML_PROPERTIES = ".idea/caches/iml.properties"
    var lastDevices = ""
    val devices = ArrayList<Pair<String, IDevice>>()
    val comboxs = ArrayList<JComboBox<String>>()
    val ftModules = mutableMapOf<String, Boolean?>()

    init {
        AndroidDebugBridge.addDeviceChangeListener(this)
        LocalFileSystem.getInstance().addVirtualFileListener(this)
    }

    override fun contentsChanged(event: VirtualFileEvent) {
        if (event.fileName != "modules.xml" || !event.file.path.endsWith(".idea/modules.xml")) return
        val moduleXml = event.file
        val target = ProjectManager.getInstance().openProjects.find { moduleXml.path.startsWith(it.basePath ?: "") }
                ?: return
        target.save()
        //格式化iml文件
        if (checkIfFormat(target)) formatModule(target, moduleXml)
    }

    fun checkIfFormat(target: Project?): Boolean {
        target ?: return false
        var format = ftModules[target.basePath]
        if (format == null) {
            val properties = readProperties(File(target.basePath, IDE_PROPERTIES))
            format = "Y" == properties.getProperty(NewImportDialog.FORMAT_KEY, "Y")
            ftModules[target.basePath!!] = format
        }
        return format
    }

    fun formatModule(target: Project, moduleXml: VirtualFile?) {
        val projectXmlFile = File(target.basePath, "templet/project.xml")
        if (moduleXml == null || !projectXmlFile.exists()) return

        val ins = moduleXml.inputStream.reader()
        val txtLines = ins.readLines()
        ins.close()
        val tag = "<!--end-->"
        if (txtLines.lastOrNull()?.endsWith(tag) == true) return
        invokeLaterOnWriteThread(Runnable {
            val iml = ".iml"
            val rex = Regex("group=\".*\"")
            val groups = XmlHelper.parseProjectXml(projectXmlFile).allSubModules().map { it.name to it.path.substringBeforeLast("/").let { p -> if (p.startsWith("/")) p.substring(1) else p } }.toMap()
            val newTxt = txtLines.map {
                val i = it.indexOf(iml)
                it.replace(rex, if (i > 0) groups[it.substring(0, i).substringAfterLast("/")]?.let { l -> "group=\"$l\"" }
                        ?: "" else "")
            }

            val txt = newTxt.joinToString("\n") + "\n<!--${Date().toLocaleString()}--> $tag"
            val w = moduleXml.getOutputStream(object : SafeWriteRequestor {}).writer()
            w.write(txt)
            w.close()
            target.save()

        })
    }

    fun invokeLaterOnWriteThread(action: Runnable) = ApplicationManager.getApplication().invokeLater {
        ApplicationManager.getApplication().runWriteAction(action)
    }

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

    fun getSelectDevice(comboBox: JComboBox<*>): IDevice? {
        val id = comboBox.selectedItem?.toString() ?: ""
        //保存最后一次选择项
        return devices.find { it.first == id }?.second?.apply { lastDevices = serialNumber }
    }

    fun addDevicesComboBox(project: Project, comboBox: JComboBox<String>) {
        val ds = AndroidSdkUtils.getDebugBridge(project)?.devices?.map { Pair(it.getDevicesName(), it) }?.sortedByDescending { it.second.serialNumber == lastDevices }
        if (ds != null) {
            devices.clear()
            devices.addAll(ds)
        }
        devices.forEach { comboBox.addItem(it.first) }
        comboxs.add(comboBox)
    }

    fun IDevice.getDevicesName() = avdName
            ?: getProperty("ro.product.model")?.let { "${getProperty("ro.product.manufacturer")} $it" }
            ?: serialNumber

    fun removeDevicesComboBox(comboBox: JComboBox<String>) = comboxs.remove(comboBox)

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

    fun base64Encode(source: String) = String(Base64.getEncoder().encode(source.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)
    fun base64Decode(source: String) = String(Base64.getDecoder().decode(source.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)
}
