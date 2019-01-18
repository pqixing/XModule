package com.pqixing.intellij.adapter

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.util.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

open class JListSelectAdapter(val jList: JList<JListInfo>) : AbstractListModel<JListInfo>(), ListCellRenderer<JListInfo>, ListSelectionListener {
    var startIndex = -1
    var endIndex = -1
    public var selectListener: JlistSelectListener? = null
    var label = JLabel().apply {
        font = Font("宋体", Font.PLAIN, 14)
        setSize(350, 30)
    }
    var panel = JPanel().apply {
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
        val t = LinkedList<JListInfo>()
        if (reelection) for (i in start..end) t.add(datas[i]) else t.add(datas[nowSelect])
        //如果外部处理了选择事件,则,内部不再选中
        if (selectListener?.onItemSelect(jList, this, t) != true) {
            t.forEach { it.select = !it.select }
            jList.invalidate()
        }
    }

    public val datas = mutableListOf<JListInfo>()
    var reelection = true


    init {
        jList.addListSelectionListener(this)
        jList.cellRenderer = this
    }

    open fun setDatas(ds: List<JListInfo>) {
        datas.clear()
        datas.addAll(ds)
        jList.model = this
//        jList.setSize(jList.width, datas.size * 30+15)
    }

    fun cover(ds: List<String>) = ds.map { JListInfo(title = it) }
    override fun getElementAt(p0: Int): JListInfo = datas[p0]

    override fun getSize(): Int = datas.size

    override fun getListCellRendererComponent(p0: JList<out JListInfo>?, info: JListInfo, p2: Int, p3: Boolean, p4: Boolean): Component? {
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
            text = prefix + info.title + "   " + info.log
            val revers = endIndex != -1 && p2 in Math.min(startIndex, endIndex)..Math.max(startIndex, endIndex)
            val select = if (revers) !info.select else info.select
            background = if (select) Color.LIGHT_GRAY else Color.WHITE
        }
        return panel
    }
}

interface JlistSelectListener {
    fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean
}

class JListInfo(var title: String = "", var log: String = "", var staue: Int = 0, var select: Boolean = false) {
    val infoId = i++
    override fun equals(other: Any?): Boolean {
        return (other as? JListInfo)?.infoId == infoId
    }

    override fun toString(): String {
        return "JListInfo(title='$title', log='$log', staue=$staue, select=$select, infoId=$infoId)"
    }

    companion object {
        var i = 1
    }

}