package com.pqixing.intellij.ui

import com.intellij.openapi.project.Project
import com.pqixing.intellij.ui.adapter.JListInfo
import com.pqixing.intellij.ui.adapter.JListSelectAdapter
import com.pqixing.intellij.ui.adapter.JlistSelectListener
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class VersionDialog(project: Project, branches: List<String>) : BaseJDialog(project) {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var jlBranches: JList<JListInfo>? = null
    var project: Project
    var adapter: JListSelectAdapter
    private var onOk: Runnable? = null
    val selects = mutableListOf<JListInfo>()
    fun setOnOk(onOk: Runnable?) {
        this.onOk = onOk
    }

    private fun resetTag(items: List<JListInfo>): Boolean {
        selects.addAll(items.filter { !it.select })
        selects.forEach { it.log = "" }
        selects.removeAll(items.filter { it.select })
        selects.firstOrNull()?.log = "TAG"
        return false
    }

    private fun onOK() {
        onOk?.run()
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    val excludes: List<String>
        get() = selects.map { it.title }

    init {
        setContentPane(contentPane)
        isModal = false
        getRootPane().defaultButton = buttonOK
        this.project = project
        title = "Index Version From Maven"
        buttonOK!!.addActionListener { e: ActionEvent? -> onOK() }
        buttonCancel!!.addActionListener { e: ActionEvent? -> onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ e: ActionEvent? -> onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        adapter = JListSelectAdapter(jlBranches!!, true)
        adapter.setDatas(branches.map { JListInfo(it, "", 0, false) })
        adapter.selectListener = object : JlistSelectListener {
            override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {
                return resetTag(items)
            }
        }
    }
}