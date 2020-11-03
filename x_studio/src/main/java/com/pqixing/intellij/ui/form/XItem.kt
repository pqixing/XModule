package com.pqixing.intellij.ui.form

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

class XItem {
    lateinit var jItemRoot: JPanel
    lateinit var cbSelect: JCheckBox
    lateinit var tvTitle: JButton
    lateinit var tvContent: JButton
    lateinit var tvTag: JButton


    var title: String
        get() = tvTitle.text
        set(value) {
            tvTitle.text = value
        }

    var tag: String
        get() = tvTag.text
        set(value) {
            tvTag.text = value
        }
    var content: String
        get() = tvContent.text
        set(value) {
            tvContent.text = value
        }

    var select: Boolean
        get() = cbSelect.isSelected
        set(value) {
            cbSelect.isSelected = value
        }
    var selectAble: Boolean
        get() = cbSelect.isVisible
        set(value) {
            cbSelect.isVisible = value
        }

    var visible: Boolean
        get() = jItemRoot.isVisible
        set(value) {
            jItemRoot.isVisible = value
        }

    val params = mutableMapOf<String, Any?>()
    fun <T> get(key: String): T? {
        return params[key] as? T
    }


    init {
        tvTitle.addMouseListener(MouseHandle(tvTitle))

    }

    inner class MouseHandle(tvTitle: JButton) : MouseAdapter() {

        override fun mouseClicked(e: MouseEvent?) {
            super.mouseClicked(e)
            e ?: return
            val right = e.button == MouseEvent.BUTTON3
            val left = e.button == MouseEvent.BUTTON1
        }
    }
}