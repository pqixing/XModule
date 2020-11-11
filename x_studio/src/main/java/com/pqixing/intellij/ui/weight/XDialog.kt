package com.pqixing.intellij.ui.weight

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.ui.adapter.XBaseAdapter
import java.awt.Rectangle
import java.awt.event.KeyEvent
import javax.swing.*

open class XModuleDialog(e: AnActionEvent) : XEventDialog(e) {
    protected val manifest = XmlHelper.loadManifest(basePath)
    protected val config = XmlHelper.loadConfig(basePath)
}

open class XEventDialog(val e: AnActionEvent, val project: Project = e.project!!, val module: Module? = e.getData(DataKey.create<Module>("module"))) : XDialog(project) {
    protected var basePath = project.basePath ?: System.getProperty("user.home")
}

open class XDialog(project: Project?) : DialogWrapper(project, true, false) {
    protected var adapter: XBaseAdapter
    protected lateinit var content: JScrollPane
    protected lateinit var center: JPanel
    protected val cbAll: JCheckBox by lazy { getAllCheckBox().also { c -> c.addActionListener { doOnAllChange(c.isSelected) } } }

    protected open fun doOnAllChange(selected: Boolean) {

    }

    protected open fun getAllCheckBox(): JCheckBox = JCheckBox("All", null, false)


    init {
        adapter = XBaseAdapter(center)
        isModal = false
    }
    protected open fun getTitleStr(): String = ""
    open fun getAllText() = "All"

    override fun show() {
        title = getTitleStr()
        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction({ doCancelAction() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        contentPanel.registerKeyboardAction({ btnEnable(!okAction.isEnabled) }, KeyStroke.getKeyStroke(KeyEvent.VK_CAPS_LOCK, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        init()
        super.show()
    }


    protected open fun btnEnable(enable: Boolean) {
//        myCancelAction.isEnabled = enable
        myOKAction.isEnabled = enable
    }

    fun repaint(component: JComponent) {
        component.parent?.let { parent ->
            val r: Rectangle = component.bounds
            parent.repaint(r.x, r.y, r.width, r.height)
        }
        component.revalidate()
    }

    override fun createDoNotAskCheckbox(): JComponent? = cbAll
    override fun createCenterPanel(): JComponent = content
}