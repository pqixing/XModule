package com.pqixing.intellij

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import java.io.File

class XGroup : DefaultActionGroup() {

    companion object {
        fun isBasic(project: Project?): Boolean = XApp.isBasic(project)

        fun isDebug(project: Project?) = isBasic(project) && File(project?.basePath!!, "basic/.action/debug").exists()

        fun isCreator(project: Project?) = isBasic(project) && File(project?.basePath!!, "basic/.action/creator").exists()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = XApp.isBasic(e.project, true)
        e.presentation.isEnabled = true
//        (ActionManager.getInstance().getAction("DeviceAndSnapshotComboBox") as DeviceAndSnapshotComboBoxAction).getSelectedDevice(e.project!!)
        //启动后，尝试打开socket连接，接收gradle插件的通知
//        val oldUpdate = ActionManager.getInstance().getAction("Vcs.UpdateProject")
//        val field = (AbstractCommonUpdateAction::class.java).getDeclaredField("myScopeInfo").also { it.isAccessible = true }
//        val myScopeInfo = field.let { it.get(oldUpdate) }
//        if (myScopeInfo !is ScopeInfoProxy) field.set(oldUpdate, ScopeInfoProxy(myScopeInfo as ScopeInfo))

//        Vcs.UpdateProject
    }
}
