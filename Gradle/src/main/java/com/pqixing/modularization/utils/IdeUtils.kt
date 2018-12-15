package com.pqixing.modularization.utils

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.FileUtils
import java.io.File

/**
 * 用于Ide的工具
 */
object IdeUtils {

    /**
     * 输出结果，用于Ide的交互获取
     * @param exitCode 退出标记 0 表示正常退出，1表示异常退出
     */
    fun writeResult(msg: String, exitCode: Int = 0) {
        val plugin = ManagerPlugin.getManagerPlugin()
        val ideFile = File(plugin.cacheDir, "ide.log")
        val log = "${Keys.PREFIX_IDE_LOG}?${Keys.RUN_TASK_ID}=${getProperty(Keys.RUN_TASK_ID)
                ?: System.currentTimeMillis()}&exitCode=$exitCode&msg=$msg"

        Tools.println(msg)
        if (ideFile.exists() && ideFile.length() > 102400) {
            Tools.println("writeResult del ${ideFile.path}-> ${ideFile.length()}")
            FileUtils.delete(ideFile)
        }

        if (!ideFile.exists()) {
            FileUtils.writeText(ideFile, log)
        } else ideFile.appendText("\n" + log)
    }

    fun getProperty(key: String): String? = try {
        System.getProperty(key)
    } catch (e: Exception) {
        null
    }
}