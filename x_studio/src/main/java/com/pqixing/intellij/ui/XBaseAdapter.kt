package com.pqixing.intellij.ui

import com.pqixing.intellij.ui.form.XItemForm
import java.awt.Component
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.ListCellRenderer

class XBaseAdapter(val jList: JList<XData>) : AbstractListModel<XData>(), ListCellRenderer<XData> {
    val pop = PopupMenu()

    init {
        jList.cellRenderer = this
        jList.addMouseListener(MouseHandle())
        jList.add(pop)

    }

    private val sources = mutableListOf<XData>()
    private var shows = listOf<XData>()

    var filter: (d: XData) -> Boolean = { _ -> true }
        set(value) {
            field = value
            notifyData()
        }

    var onItemClick: (index: Int, data: XData) -> Unit = { _, _ -> }
    var onItemLongClick: (index: Int, data: XData) -> Unit = { _, _ -> }
    var onItemRightClick: (index: Int, data: XData) -> Unit = { _, _ -> }


    fun showItemMenu(e: MouseEvent, items: List<String>, click: (index: Int) -> Unit) {
        pop.removeAll()
        items.map { MenuItem(it) }.forEachIndexed { index, menuItem ->
            menuItem.addActionListener { click(index) }
            pop.add(menuItem)
        }
    }

    fun setData(datas: List<XData>) {
        sources.clear()
        sources.addAll(datas)
        notifyData()
    }

    fun notifyData() {
        shows = sources.filter(filter)
        jList.model = this
        jList.invalidate()
        jList.setSize(jList.width, sources.size * 30 + 15)
        XItemForm().rootPanal
    }

    inner class MouseHandle : MouseAdapter() {

        override fun mouseClicked(e: MouseEvent?) {
            super.mouseClicked(e)
            e ?: return
            val right = e.button == MouseEvent.BUTTON3
            val index = jList.selectedIndex
            e.x
        }
    }

    override fun getSize(): Int = shows.size

    override fun getElementAt(index: Int): XData = shows[index]

    override fun getListCellRendererComponent(list: JList<out XData>?, value: XData?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        TODO("Not yet implemented")
    }
}

data class XData(var check: Boolean = false, var title: String, var desc: String, var result: String, var select: Boolean, var state: Int)

