package com.pqixing.intellij.ui.form

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.pqixing.intellij.ui.adapter.XBaseAdapter
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.KeyStroke

open class XDialog(project: Project?) : DialogWrapper(project, true, false) {
    var adapter: XBaseAdapter
    private lateinit var scroll: JScrollPane
    private lateinit var center: JPanel

    protected var myAll: ActionListener? = null

    init {
        adapter = XBaseAdapter(center)
        isModal = false
    }

    override fun show() {
        if (myAll != null) {
            setDoNotAskOption(object : DoNotAskOption.Adapter() {
                override fun getDoNotShowMessage(): String = "All"
                override fun rememberChoice(isSelected: Boolean, exitCode: Int) {
                }
            })
        }
        init()
        myCheckBoxDoNotShowDialog?.addActionListener(myAll)
        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction({ doCancelAction() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        contentPanel.registerKeyboardAction({ a: ActionEvent? -> btnEnable(!okAction.isEnabled) }, KeyStroke.getKeyStroke(KeyEvent.VK_CAPS_LOCK, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        super.show()
    }

    protected open fun btnEnable(enable: Boolean) {
        myCancelAction.isEnabled = enable
        myOKAction.isEnabled = enable
    }

    fun repaint(component: JComponent) {
        component.parent?.let { parent ->
            val r: Rectangle = component.bounds
            parent.repaint(r.x, r.y, r.width, r.height)
        }
        component.revalidate()
    }

    override fun createCenterPanel(): JComponent = scroll

//    override fun createCenterPanel(): JComponent? {
//        val frame = JFrame("XDialog")
//        frame.contentPane = jpRoot
//        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//        frame.pack()
//        frame.setLocationRelativeTo(null)
//        frame.isVisible = true
//        return frame
//    }
}