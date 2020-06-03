package com.pqixing.intellij.utils

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.tools.apk.analyzer.AaptInvoker
import com.android.tools.apk.analyzer.AndroidApplicationInfo
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil
import com.android.tools.idea.sdk.AndroidSdks
import com.pqixing.creator.utils.LogWrap
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.help.XmlHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.tools.PropertiesUtils
import groovy.lang.GroovyClassLoader
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.util.*
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.TransferHandler

object UiUtils : AndroidDebugBridge.IDeviceChangeListener {
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
    var lastModify = 0L

    init {
        AndroidDebugBridge.addDeviceChangeListener(this)
    }

    fun formatProject(project: Project?) = ApplicationManager.getApplication().invokeLater {
        val p = project ?: return@invokeLater
        ApplicationManager.getApplication().runWriteAction {
            p.save()
            val xml = File(p.basePath, ".idea/modules.xml")
            if (lastModify == xml.lastModified()) return@runWriteAction

            xml.writeText(xml.readText().replace(Regex("group=\".*\""), ""))
            VfsUtil.findFileByIoFile(xml, true)?.refresh(false, false)
            lastModify = xml.lastModified()

            collectImls(project, p)
//            ApplicationManager.getApplication().invokeLater {
//                Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Build Finish", "format project", NotificationType.INFORMATION).notify(p)
//            }
        }
    }

    private fun collectImls(project: Project, p: Project) {
        val imls = File(project.basePath, IML_PROPERTIES)
        val pimls = PropertiesUtils.readProperties(imls)

        val projectXmlFile = File(p.basePath, "templet/project.xml")
        if (!projectXmlFile.exists()) return

        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(File(p.basePath, "Config.java"))
        val newInstance = clazz.newInstance()
        val codeRoot = clazz.getField("codeRoot").get(newInstance).toString()

        val manager = ModuleManager.getInstance(project)

        manager.modules.toList().forEach {
            val path = getImlPath(codeRoot, projectXml, it.name)
            if (path != null) {
                pimls[path] = it.moduleFilePath
            }
        }
        PropertiesUtils.writeProperties(imls, pimls)
    }

    /**
     * 加载模块
     */
    fun loadModule(manager: ModuleManager, filePath: String) {
        if (File(filePath).exists()) try {
            manager.loadModule(filePath)
        } finally {
        }
    }

    fun getImlPath(codeRoot: String, projectXml: ProjectXmlModel, title: String): String? {
        val path = projectXml.findSubModuleByName(title)?.path ?: return null
        return "$codeRoot/$path"
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
