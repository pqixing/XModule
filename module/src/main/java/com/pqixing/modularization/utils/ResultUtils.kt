package com.pqixing.modularization.utils

import com.pqixing.help.Tools
import com.pqixing.modularization.Keys
import org.gradle.api.GradleException


/**
 * 用于Ide的工具
 */
object ResultUtils {

    fun thow(error: String?) {
        throw GradleException(error)
    }

    /**
     * 输出结果，用于Ide的交互获取
     * @param exitCode 退出标记 0 表示正常退出，1表示异常退出
     */
    fun writeResult(msg: String, exitCode: Int = 0, exit: Boolean = exitCode != 0) = writeResult(mapOf("msg" to msg), exitCode, exit)

    fun writeResult(params: Map<String, String>, exitCode: Int = 0, exit: Boolean = exitCode != 0) {
        val sb = StringBuilder(Keys.PREFIX_IDE_LOG).append("?")
        if (params.isNotEmpty()) {
            params.forEach { (t, u) -> sb.append(t).append("=").append(u).append("$") }
            sb.deleteCharAt(sb.length - 1)
        }
        val msg = sb.toString()
        Tools.println(msg)
        if (exit) throw  GradleException(msg)
    }
}
