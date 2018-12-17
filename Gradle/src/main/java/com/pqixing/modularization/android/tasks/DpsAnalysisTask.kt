package com.pqixing.modularization.android.tasks

import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BaseTask
import java.io.File

/**
 * 依赖对比分析
 * 1,生成模块的依赖分析文件
 * 2，
 */
open class DpsAnalysisTask : BaseTask() {


    override fun runTask() {
        val plugin = AndroidPlugin.getPluginByProject(project)
        val cacheDir = plugin.cacheDir
        val outFile = File(cacheDir, Keys.TXT_DPS_ANALYSIS)
    }

}