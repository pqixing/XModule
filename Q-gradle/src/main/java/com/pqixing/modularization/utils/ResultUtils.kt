package com.pqixing.modularization.utils

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.GradleException
import java.io.File
import java.util.*

/**
 * 用于Ide的工具
 */
object ResultUtils {

    //是否是通过ide调用
    val ide = "ide" == TextUtils.getSystemEnv("syncType")

    /**
     * 输出结果，用于Ide的交互获取
     * @param exitCode 退出标记 0 表示正常退出，1表示异常退出
     */
    fun writeResult(msg: String, exitCode: Int = 0, exit: Boolean = exitCode != 0) {
        Tools.println(msg)
        if (ide) {
            val log = "${Keys.PREFIX_IDE_LOG}?${Keys.RUN_TASK_ID}=${getProperty(Keys.RUN_TASK_ID)
                    ?: System.currentTimeMillis()}&endTime=${System.currentTimeMillis()}&exitCode=$exitCode&msg=$msg"
            val plugin = ManagerPlugin.getPlugin()
            var logCount = 0
            do {
                val ideFile = File(plugin.rootDir, ".idea/modularization.log${logCount++}")
                //只保留10条记录
                val logs = LinkedList<String>()
                FileUtils.readText(ideFile)?.lines()?.apply {
                    for (i in 0 until (Math.min(19, size))) {
                        logs.addFirst(get(size - 1 - i))
                    }
                }
                logs.add(log)
                FileUtils.writeText(ideFile, logs.joinToString("\n"))
            } while (ideFile.readText() != log && logCount <= 6)//如果写入失败并且次数小于6次,则尝试继续写入
        }
        if (exit) {
            Thread.sleep(500)
            throw  GradleException(msg)
        }
    }

    fun getProperty(key: String): String? = try {
        System.getProperty(key)
    } catch (e: Exception) {
        null
    }
}