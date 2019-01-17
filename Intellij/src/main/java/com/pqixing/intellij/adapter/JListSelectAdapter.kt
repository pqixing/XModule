package com.pqixing.intellij.adapter

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class JListSelectAdapter(val jList: JList<*>) : AbstractListModel<JListInfo>(), ListCellRenderer<JListInfo>, ListSelectionListener {
    var startIndex = -1
    var endIndex = -1
    var label = JLabel().apply {
        font = Font("宋体", Font.PLAIN, 16)
        setSize(350, 30)
    }
    var panel = JPanel().apply {
        setSize(250, 30)
        layout = BorderLayout()
        border = BevelBorder(BevelBorder.LOWERED)
        add(label)
    }

    override fun valueChanged(event: ListSelectionEvent) {
        val nowSelect = jList.selectedIndex
        if (startIndex < 0) startIndex = nowSelect
        if (event.valueIsAdjusting) {
            endIndex = nowSelect
            return
        }
        val start = Math.min(startIndex, nowSelect)
        val end = Math.max(startIndex, nowSelect)
        endIndex = -1
        startIndex = -1
        jList.clearSelection()
        if (start < 0) return
        if (reelection) for (i in start..end) {
            with(datas[i]) {
                select = !select
            }
        } else datas[nowSelect].select = !datas[nowSelect].select
        jList.invalidate()
    }

    val datas = mutableListOf<JListInfo>()
    var reelection = true


    init {
        jList.addListSelectionListener(this)
    }

    fun setDatas(ds: List<JListInfo>) {
        datas.clear()
        datas.addAll(ds)
        jList.setSize(jList.width, datas.size * 30)
        jList.model = this
    }

    fun cover(ds: List<String>) = ds.map { JListInfo(title = it) }
    override fun getElementAt(p0: Int): JListInfo = datas[p0]

    override fun getSize(): Int = datas.size

    override fun getListCellRendererComponent(p0: JList<out JListInfo>?, info: JListInfo, p2: Int, p3: Boolean, p4: Boolean): Component? {
//        jPanel.background = Color.RED
        label.apply {

            isOpaque = true
            info.staue = p2 % 4
            foreground = when (info.staue) {
                1 -> Color.BLUE
                2 -> Color.RED
                else -> Color.BLACK
            }
            val prefix = when (info.staue) {
                1 -> "√  "
                2 -> "×  "
                3 -> "--- "
                else -> "     "
            }
            text = prefix + info.title
            val revers = endIndex != -1 && p2 in Math.min(startIndex, endIndex)..Math.max(startIndex, endIndex)
            val select = if (revers) !info.select else info.select
            background = if (select) Color.LIGHT_GRAY else Color.WHITE
        }
        return panel
    }
}


data class JListInfo(var title: String = "", var log: String = "", var staue: Int = 0, var select: Boolean = false)