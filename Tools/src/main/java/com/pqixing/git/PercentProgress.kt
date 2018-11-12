package com.pqixing.git

import com.pqixing.Tools
import com.pqixing.interfaces.ILog
import com.sun.org.apache.xml.internal.security.Init

import org.eclipse.jgit.lib.ProgressMonitor
import java.text.DecimalFormat

class PercentProgress @JvmOverloads constructor(private val logger: ILog? = Tools.logger, val space: Float = 5f) : ProgressMonitor {
    private var title: String? = null
    private var last: Int = 0
    private var total: Int = 0

    private var lastLogPercent = 0f;
    private val d = DecimalFormat("##0.00")
    //百分比间隔

    override fun start(totalTasks: Int) {
    }

    override fun beginTask(title: String, totalWork: Int) {
        this.title = title
        this.total = totalWork
        last = 0
        lastLogPercent = 0f
        logger?.println("$title -> $totalWork")
    }

    override fun update(completed: Int) {
        last += completed
        val newPercent = last * 100F / total

        if (newPercent - lastLogPercent >= space) {
            logger?.println("$title -> [$last/$total ${newPercent.toInt()}%]")
            lastLogPercent = newPercent
        }
    }

    override fun endTask() {

    }

    override fun isCancelled(): Boolean {
        return false
    }
}
