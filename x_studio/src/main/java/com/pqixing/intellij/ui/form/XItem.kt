package com.pqixing.intellij.ui.form

import java.awt.Color
import java.awt.MenuItem
import java.awt.Point
import java.awt.PopupMenu
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class XItem {
    companion object {
        const val KEY_SUCCESS = "√ "
        const val KEY_ERROR = "X "
        const val KEY_WAIT = "- "
        fun state(sucess: Boolean?) = when (sucess) {
            true -> KEY_SUCCESS
            false -> KEY_ERROR
            else -> KEY_WAIT
        }

        val wait = Color(236, 117, 0)
        val error = Color(236, 0, 0)
        val success = Color(0, 187, 18)
    }

    lateinit var jItemRoot: JPanel
    lateinit var cbSelect: JCheckBox
    lateinit var tvTitle: JLabel
    lateinit var tvContent: JLabel
    lateinit var tvTag: JLabel

    var popMenu: List<MenuItem>? = null
    val left: (c: JComponent, e: MouseEvent) -> Unit = { _, _ -> cbSelect.isSelected = !cbSelect.isSelected }
    val right: (c: JComponent, e: MouseEvent) -> Unit = { c, e ->
        val point = Point(e.x, e.y)
        if (popMenu != null) {
            c.showPop(popMenu!!, point)
        } else {
            val label = c.getComponentAt(e.x, e.y) as? JLabel
            if (label != null) c.showPop(popMenu ?: listOf(MenuItem(label.text)), Point(e.x, e.y))
        }

    }

    val normal = tvTag.foreground

    var title: String
        get() = tvTitle.text
        set(value) {
            tvTitle.text = value
        }

    var tag: String
        get() = tvTag.text
        set(value) {
            tvTag.text = value
            tvTag.foreground = when {
                value.startsWith(KEY_SUCCESS) -> success
                value.startsWith(KEY_ERROR) -> error
                value.startsWith(KEY_WAIT) -> wait
                else -> normal
            }
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
        jItemRoot.addMouseClick(left, right)
    }
}

fun JComponent.showPop(menu: List<String>, point: Point, click: (index: Int) -> Unit) = showPop(menu.mapIndexed { i: Int, s: String -> MenuItem(s).also { it.addActionListener { click(i) } } }, point)

fun JComponent.showPop(menu: List<MenuItem>, point: Point) {
    if (menu.isEmpty()) return
    val pop = PopupMenu()
    this.add(pop)
    menu.forEach { pop.add(it) }
    pop.show(this, point.x, point.y)
}

fun JComponent.addMouseClickL(left: (c: JComponent, e: MouseEvent) -> Unit) = addMouseClick(left, { _, _ -> })
fun JComponent.addMouseClickR(right: (c: JComponent, e: MouseEvent) -> Unit) = addMouseClick({ _, _ -> }, right)
fun JComponent.addMouseClick(left: (c: JComponent, e: MouseEvent) -> Unit, right: (c: JComponent, e: MouseEvent) -> Unit = { _, _ -> }) = MouseHandle(this, left, right)

class MouseHandle(val component: JComponent, val left: (c: JComponent, e: MouseEvent) -> Unit, val right: (c: JComponent, e: MouseEvent) -> Unit = { _, _ -> }) : MouseAdapter() {
    var otherClick: (c: JComponent, e: MouseEvent) -> Unit = { _, _ -> }

    init {
        component.addMouseListener(this)
    }

    override fun mouseClicked(e: MouseEvent?) {
        super.mouseClicked(e)
        e ?: return
        when (e.button) {
            MouseEvent.BUTTON3 -> right(component, e)
            MouseEvent.BUTTON1 -> left(component, e)
            else -> otherClick(component, e)
        }
    }
}
