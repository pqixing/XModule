package com.pqixing.git

import com.pqixing.Tools
import com.pqixing.interfaces.ILog
import com.sun.org.apache.xml.internal.security.Init

import org.eclipse.jgit.lib.ProgressMonitor
import java.text.DecimalFormat

class PercentProgress @JvmOverloads constructor(private val logger: ILog? = Tools.logger, val space: Float = 1500f) : ProgressMonitor {
    private var title: String? = null
    private var last: Int = 0
    private var total: Int = 0

    private var lastLogTime = 0L;
    private val d = DecimalFormat("##0.00")
    //百分比间隔

    override fun start(totalTasks: Int) {
    }

    override fun beginTask(title: String, totalWork: Int) {
        this.title = title
        this.total = totalWork
        last = 0
        lastLogTime = 0L
        logger?.println("beginTask -> $title : $totalWork")
    }

    override fun update(completed: Int) {
        last += completed
        val t = System.currentTimeMillis()
        if (last == total || t - lastLogTime >= space) {
            lastLogTime = t
            logger?.println("          -> $last/$total : ${(last.toFloat() * 100 / total).toInt()}%")
        }
    }

    override fun endTask() {
        logger?.println("endTask   -> $title : $total")
    }

    override fun isCancelled(): Boolean {
        return false
    }
}
