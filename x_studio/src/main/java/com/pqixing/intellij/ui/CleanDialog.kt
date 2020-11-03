package com.pqixing.intellij.ui

import com.intellij.openapi.project.Project
import com.pqixing.intellij.ui.adapter.JListInfo
import com.pqixing.intellij.ui.adapter.JListSelectAdapter
import com.pqixing.intellij.utils.UiUtils

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

class CleanDialog(val project: Project, val codeRoots: List<String>, val projects: List<String>, val modules: List<String>) : BaseJDialog(project) {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var jlProjects: JList<JListInfo>? = null
    var cbCache: JCheckBox? = null
    var cbIml: JCheckBox? = null
    var cbCodeRoots: JComboBox<String>? = null
    var onOk: Runnable? = null
    var adapter: JListSelectAdapter

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { e -> onOK() }

        buttonCancel!!.addActionListener { e -> onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        title = "Clean"
        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ e -> onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        adapter = JListSelectAdapter(jlProjects!!, true)
        initCodeRoot()
        reloadData()
        cbCodeRoots?.addActionListener { reloadData() }
        cbCache?.addActionListener { reloadData() }
    }

    private fun initCodeRoot() {
        for (s in codeRoots) {
            cbCodeRoots!!.addItem(s)
        }
        cbCodeRoots!!.selectedIndex = 0
    }

    private fun reloadData() {
        val codeRoots = cbCodeRoots!!.selectedItem!!.toString().trim { it <= ' ' }
        val cleanCache = cbCache!!.isSelected
        val dir = File(project.basePath, codeRoots)
        if (dir.exists()) {
            adapter.setDatas((if (cleanCache) modules else projects).filter { File(dir, it).exists() }.map { JListInfo(it, select = true) })
        } else adapter.setDatas(emptyList())
    }

    private fun onOK() {
        // add your code here
        dispose()
        if (onOk != null) onOk!!.run()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
