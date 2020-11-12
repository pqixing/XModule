package com.android.tools.idea.run.deployment

import com.android.ddmlib.IDevice
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project

object DeviceGet {
    fun getDevice(project: Project): IDevice? {
        //DeviceAndSnapshotComboBoxAction
        return kotlin.runCatching {
            val action = ActionManager.getInstance().getAction("DeviceAndSnapshotComboBox")
            val method = action.javaClass.getDeclaredMethod("getSelectedDevice", Project::class.java)
            method.isAccessible = true
            val device = method.invoke(action, project)
            val ddmlb = device.javaClass.superclass.getDeclaredMethod("getDdmlibDevice")
            ddmlb.isAccessible = true
            return ddmlb.invoke(device) as? IDevice?
        }.getOrNull()
    }

}