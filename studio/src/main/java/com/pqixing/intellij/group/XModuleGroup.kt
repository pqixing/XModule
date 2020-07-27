package com.pqixing.intellij.group

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.update.AbstractCommonUpdateAction
import com.intellij.openapi.vcs.update.ScopeInfo
import com.pqixing.EnvKeys
import com.pqixing.intellij.actions.ScopeInfoProxy
import com.pqixing.intellij.utils.GradleUtils
import com.pqixing.intellij.utils.UiUtils
import java.io.File

class XModuleGroup : DefaultActionGroup() {

    companion object {

        init {
            println("QToolGroup init -> ")
            //启动后，尝试打开socket连接，接收gradle插件的通知
            GradleUtils.tryInitSocket(GradleUtils.defPort)
            UiUtils.checkIfFormat(null)
        }

        fun hasBasic(project: Project?): Boolean = File(project?.basePath, EnvKeys.XML_MANIFEST).exists()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
//        (ActionManager.getInstance().getAction("DeviceAndSnapshotComboBox") as DeviceAndSnapshotComboBoxAction).getSelectedDevice(e.project!!)
        //启动后，尝试打开socket连接，接收gradle插件的通知
        GradleUtils.tryInitSocket(GradleUtils.defPort)
        val oldUpdate = ActionManager.getInstance().getAction("Vcs.UpdateProject")
        val field = (AbstractCommonUpdateAction::class.java).getDeclaredField("myScopeInfo").also { it.isAccessible = true }
        val myScopeInfo = field.let { it.get(oldUpdate) }
        if (myScopeInfo !is ScopeInfoProxy) field.set(oldUpdate, ScopeInfoProxy(myScopeInfo as ScopeInfo))

//        Vcs.UpdateProject
    }
}
