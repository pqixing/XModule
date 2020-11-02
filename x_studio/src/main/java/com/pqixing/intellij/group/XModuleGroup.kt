package com.pqixing.intellij.group

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.pqixing.intellij.XApp
import com.pqixing.intellij.utils.GradleUtils

class XModuleGroup : DefaultActionGroup() {

    init {
       XApp
    }

    companion object {
        inline fun hasBasic(project: Project?): Boolean = XApp.hasBasic(project)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = hasBasic(e.project)
//        (ActionManager.getInstance().getAction("DeviceAndSnapshotComboBox") as DeviceAndSnapshotComboBoxAction).getSelectedDevice(e.project!!)
        //启动后，尝试打开socket连接，接收gradle插件的通知
//        val oldUpdate = ActionManager.getInstance().getAction("Vcs.UpdateProject")
//        val field = (AbstractCommonUpdateAction::class.java).getDeclaredField("myScopeInfo").also { it.isAccessible = true }
//        val myScopeInfo = field.let { it.get(oldUpdate) }
//        if (myScopeInfo !is ScopeInfoProxy) field.set(oldUpdate, ScopeInfoProxy(myScopeInfo as ScopeInfo))

//        Vcs.UpdateProject
    }
}
