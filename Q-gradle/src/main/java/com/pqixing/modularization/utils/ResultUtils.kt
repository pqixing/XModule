package com.pqixing.modularization.utils

import com.pqixing.EnvKeys
import com.pqixing.Tools
import com.pqixing.getEnvValue
import com.pqixing.modularization.Keys
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.GradleException
import java.io.File
import java.net.Socket
import java.util.*


/**
 * 用于Ide的工具
 */
object ResultUtils {

    //是否是通过ide调用
    val ide = "ide" == EnvKeys.syncType.getEnvValue()

    /**
     * 输出结果，用于Ide的交互获取
     * @param exitCode 退出标记 0 表示正常退出，1表示异常退出
     */
    fun writeResult(msg: String, exitCode: Int = 0, exit: Boolean = exitCode != 0) {
        Tools.println(msg)
        val log = "${Keys.PREFIX_IDE_LOG}?${Keys.RUN_TASK_ID}=${getProperty(Keys.RUN_TASK_ID)
                ?: System.currentTimeMillis()}&endTime=${System.currentTimeMillis()}&exitCode=$exitCode&msg=$msg"

        if (ide && !writeToSocket(log)) {
            val rootDir = ManagerPlugin.getPlugin().rootDir
            var logCount = 0
            do {
                val ideFile = File(rootDir, ".idea/modularization.log${logCount++}")
                //只保留10条记录
                val logs = LinkedList<String>()
                FileUtils.readText(ideFile)?.lines()?.apply {
                    for (i in 0 until (Math.min(19, size))) {
                        logs.addFirst(get(size - 1 - i))
                    }
                }
                logs.add(log)
                FileUtils.writeText(ideFile, logs.joinToString("\n"))

                Thread.sleep(500)
            } while (!ideFile.readText().endsWith(log) && logCount <= 7)//如果写入失败并且次数小于6次,则尝试继续写入
        }
        if (exit) throw  GradleException(msg)
    }

    /**
     * 尝试通过socket写入数据
     */
    private fun writeToSocket(log: String) = try {
        val socket = Socket("localhost", getProperty("ideSocketPort")?.toInt() ?: 8890)
        val outputStream = socket.getOutputStream().bufferedWriter()//获取一个输出流，向服务端发送信息
        outputStream.write(log + "\n")
        outputStream.flush()
        outputStream.close()
        socket.close()
        true
    } catch (e: Exception) {
        false
    }

    fun getProperty(key: String): String? = try {
        System.getProperty(key)
    } catch (e: Exception) {
        null
    }
}