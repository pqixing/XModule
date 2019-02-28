package com.pqixing.modularization.utils

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.GradleException
import java.io.File

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
    fun writeResult(msg: String, exitCode: Int = 0,exit:Boolean = exitCode!=0) {
        Tools.println(msg)
        if(ide) {
            val plugin = ManagerPlugin.getPlugin()
            val ideFile = File(plugin.rootDir, ".idea/modularization.log")
            val log = "${Keys.PREFIX_IDE_LOG}?${Keys.RUN_TASK_ID}=${getProperty(Keys.RUN_TASK_ID)
                    ?: System.currentTimeMillis()}&endTime=${System.currentTimeMillis()}&exitCode=$exitCode&msg=$msg"
            if (ideFile.exists() && ideFile.length() > 102400) {
                Tools.println("writeResult del ${ideFile.path}-> ${ideFile.length()}")
                FileUtils.delete(ideFile)
            }

            if (!ideFile.exists()) {
                FileUtils.writeText(ideFile, log)
            } else ideFile.appendText("\n" + log)
        }
        if (exit) {
            throw  GradleException(msg)
        }
    }

    fun getProperty(key: String): String? = try {
        System.getProperty(key)
    } catch (e: Exception) {
        null
    }
}