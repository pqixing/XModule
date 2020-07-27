package com.pqixing.modularization.utils

import com.pqixing.EnvKeys
import com.pqixing.Tools
import com.pqixing.getEnvValue
import com.pqixing.modularization.Keys
import org.gradle.api.GradleException
import java.io.File
import java.net.Socket
import java.util.*


/**
 * 用于Ide的工具
 */
object ResultUtils {
    var lastIdePort = -1
    val defPort = 8451


    //是否是通过ide调用
    val ide
        get() = "ide" == EnvKeys.syncType.getEnvValue()

    fun thow(error: String?) {
        throw GradleException(error)
    }

    /**
     * 输出结果，用于Ide的交互获取
     * @param exitCode 退出标记 0 表示正常退出，1表示异常退出
     */
    fun writeResult(msg: String, exitCode: Int = 0, exit: Boolean = exitCode != 0) {
        val log = "${Keys.PREFIX_IDE_LOG}?${Keys.RUN_TASK_ID}=${getProperty(Keys.RUN_TASK_ID)
                ?: System.currentTimeMillis()}&endTime=${System.currentTimeMillis()}&exitCode=$exitCode&msg=$msg"

        if (ide) writeToSocket(log, getProperty("ideSocketPort")?.toInt() ?: defPort)
        if (exit) throw  GradleException(msg)
    }

    fun ideProject(rootDir: File) = File(rootDir, ".idea").exists()

    fun notifyIde(rootDir: File, params: MutableMap<String, String>) {
        //非idea工程，不处理
        if (!ideProject(rootDir)) return
        params["url"] = String(Base64.getEncoder().encode(rootDir.absolutePath.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)
        val str = "${Keys.PREFIX_IDE_NOTIFY}?${params.map { "${it.key}=${it.value}" }.joinToString("&")}"
        if (lastIdePort > 0) writeToSocket(str, lastIdePort)
        else {
            var cur = defPort
            while ((cur - defPort) < 20) if (writeToSocket(str, cur++)) break
        }
    }

    /**
     * 尝试通过socket写入数据
     */
    private fun writeToSocket(log: String, port: Int) = try {
        val socket = Socket("localhost", port)
        val outputStream = socket.getOutputStream().bufferedWriter()//获取一个输出流，向服务端发送信息
        outputStream.write(log + "\n")
        outputStream.flush()
        outputStream.close()
        socket.close()
        Tools.println("writeToSocket $port -> $log")
        lastIdePort = port
        true

    } catch (e: Exception) {
        Tools.println("writeToSocket $port -> $log ${e.message}")
        false
    }

    fun getProperty(key: String): String? = try {
        System.getProperty(key)
    } catch (e: Exception) {
        null
    }
}
