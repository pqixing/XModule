package com.pqixing.intellij.ui.form

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.pqixing.intellij.ui.adapter.XBaseAdapter
import java.awt.Rectangle
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

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
        super.show()
    }

    override fun doOKAction() {
//        super.doOKAction()
        val item = XItem()
        item.tvTitle.text = "${adapter.getSize()}----"
        item.tvContent.text = "this  is  content "
        item.tvTag.text = "Y"
        item.tvTag.addActionListener {
            item.tvContent.text = item.tvContent.text + item.tvContent.text
            item.tvTag.text = item.tvTag.text + item.tvTag.text
        }
        item.tvContent.addActionListener {
//                XApp.log("add to e-------")
//                Tools.printError(0, "add event ->>>>")
//                lg.log(Level.WARNING, "add action --------test")
//                pop.removeAll()
//                arrayOf("pop1", "pop2").map { MenuItem(it) }.forEachIndexed { index, menuItem ->
//                    menuItem.addActionListener { lg.log(Level.WARNING, "pop click --------") }
//                    pop.add(menuItem)
//                }
//                pop.show(item.tvContent, 0, 0)
        }
        adapter.add(listOf(item))
    }

    override fun doCancelAction() {
        super.doCancelAction()

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