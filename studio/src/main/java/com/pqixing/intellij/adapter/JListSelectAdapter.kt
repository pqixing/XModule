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

open class JListSelectAdapter(val jList: JList<JListInfo>, var boxVisible: Boolean) : AbstractListModel<JListInfo>(), ListCellRenderer<JListInfo>, ListSelectionListener {
    private var startIndex = -1
    private var endIndex = -1
    public var selectListener: JlistSelectListener? = null
    val selectColor = Color(128,128,128)
    private var label = JLabel().apply {
        font = Font("宋体", Font.PLAIN, 14)
//        setSize(100, 30)
    }

    private var box = JCheckBox()
    private var log = JLabel().apply {
        font = Font("宋体", Font.PLAIN, 12)
        setSize(100, 30)
    }
    private var panel = JPanel().apply {
        layout = BorderLayout()
        border = BevelBorder(BevelBorder.LOWERED)
        add(label, BorderLayout.CENTER)
        add(box, BorderLayout.WEST)
        add(log, BorderLayout.EAST)
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
        updateUI()
    }

    fun updateUI() {
        jList.model = this
        jList.invalidate()
        jList.setSize(jList.width, datas.size * 30 + 15)
    }

    fun cover(ds: List<String>) = ds.map { JListInfo(title = it) }
    override fun getElementAt(p0: Int): JListInfo = datas[p0]

    override fun getSize(): Int = datas.size
    val wanming = Color(236, 117, 0)
    val error = Color(236, 0, 0)
    val success = Color(0, 187, 18)
    val normal = label.foreground

    override fun getListCellRendererComponent(p0: JList<out JListInfo>?, info: JListInfo, p2: Int, p3: Boolean, p4: Boolean): Component? {
        label.text = "   " + info.title
        val logStr = if (info.log.length > 30) info.log.substring(info.log.length - 30) else info.log
        log.text = logStr + when (info.staue) {
            1 -> " √ "
            2 -> "--"
            3 -> " ×  "
            else -> "   "
        }
        log.foreground = when (info.staue) {
            3 -> error
            1 -> success
            0 -> normal
            else -> wanming
        }
        log.isOpaque = info.staue == 3

        val revers = endIndex != -1 && p2 in Math.min(startIndex, endIndex)..Math.max(startIndex, endIndex)
        box.isSelected = if (revers) !info.select else info.select
        box.isVisible = boxVisible
        if(!boxVisible) {//#808080
            panel.background = if(box.isSelected) selectColor else label.background
        }
        return panel
    }
}

interface JlistSelectListener {
    fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean
}

class JListInfo(var title: String = "", var log: String = "", var staue: Int = 0, var select: Boolean = false) {
    var infoId = (i++).toString()
    var data:Any?=null
    override fun equals(other: Any?): Boolean {
        return (other as? JListInfo)?.infoId == infoId
    }

    override fun toString(): String {
        return "$title $log"
    }

    companion object {
        var i = 1
    }

}