package com.pqixing.intellij.ui.form

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.pqixing.help.Tools
import com.pqixing.intellij.XApp
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.Rectangle
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*

class XDialog(project: Project?) : DialogWrapper(project, true, false) {
    private lateinit var jpRoot: JPanel
    private lateinit var jsContent: JScrollPane
    private lateinit var jpTop: JPanel
    private lateinit var jpBottom: JPanel
    private lateinit var jpRight: JPanel
    private lateinit var jpLeft: JPanel
    private lateinit var jpCenter: JPanel
    private lateinit var btnOK: JButton
    val pop = PopupMenu()

    init {
        val lg = Logger.getLogger("")
        jpCenter.layout = VerticalFlowLayout()
        isModal = false
        peer.setContentPane(jpRoot)
        title = "Title"
        jpCenter.add(pop)
        btnOK.addActionListener {
            val button =JButton("${jpCenter.componentCount}-----------------").apply {
                addActionListener {
                    XApp.log("add to e-------")
                    Tools.printError(0,"add event ->>>>")
                    lg.log(Level.WARNING, "add action --------test")
                    pop.removeAll()
                    arrayOf("pop1", "pop2").map { MenuItem(it) }.forEachIndexed { index, menuItem ->
                        menuItem.addActionListener { lg.log(Level.WARNING, "pop click --------") }
                        pop.add(menuItem)
                    }
                    pop.show(this, 0, 0)
                }
            }
            jpCenter.add(button)
           repaint(button)

        }
        setResizable(true)
    }

    fun repaint(component: JComponent) {
        component.parent?.let { parent ->
            val r: Rectangle = component.bounds
            parent.repaint(r.x, r.y, r.width, r.height)
        }
        component.revalidate()
    }

    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    override fun createCenterPanel(): JComponent = jpRoot

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