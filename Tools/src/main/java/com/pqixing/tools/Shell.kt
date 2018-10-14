package com.pqixing.tools

import java.io.Closeable
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Shell {
    var logger: Logger? = null
    private val END = "-------- Shell execute end -> "
    private val START = "-------- Shell execute start -> "

    private val pool = ThreadPoolExecutor(4, 4, 30L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @JvmStatic
    fun testRun(cm: String) {
        logger?.log(cm)
    }

    @JvmStatic
    fun runSync(cmd: String, dir: File? = null, callBack: ShellCallBack? = null): String {
        val process = Runtime.getRuntime().exec(cmd, arrayOf(), dir)

        return handleResult(cmd, process, callBack)
    }

    fun runASync(cmd: String, dir: File? = null, callBack: ShellCallBack? = null) {
        val process = Runtime.getRuntime().exec(cmd, arrayOf(), dir)
        pool.execute { handleResult(cmd, process, callBack) }
    }

    private fun handleResult(cmd: String, process: Process, callBack: ShellCallBack?): String {
        logger?.log(START + cmd)
        val resultCache = LinkedBlockingQueue<String>()
        val streamIn = process.inputStream.bufferedReader()
        val streamErr = process.errorStream.bufferedReader()
        val afterRun = arrayOf(false, false)
        val readIn = Runnable {
            var str: String? = null
            do {
                str = streamIn.readLine()
                if (str != null) resultCache.put(str)
            } while (str != null)
            afterRun[0] = true
        }
        val readErr = Runnable {
            var str: String? = null
            do {
                str = streamErr.readLine()
                if (str != null) resultCache.put(str)
            } while (str != null)
            afterRun[1] = true
        }

        pool.execute(readIn)
        pool.execute(readErr)

        var line: String?
        val sb = StringBuilder()
        var lastLineTime = System.currentTimeMillis()
        while (true) {
            line = resultCache.poll()
            if (line == null) {
                if (!process.isAlive || (afterRun[0] && afterRun[1])) {
                    break
                }
                if (System.currentTimeMillis() - lastLineTime > 1000 * 15) {
                    logger?.log("process exit by time out")
                }
                continue
            } else {
                lastLineTime = System.currentTimeMillis()
                logger?.log(line)
                callBack?.call(line)
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(line)
            }
        }

        closeQuite(streamIn)
        closeQuite(streamErr)
        try {
            process.destroy()
        } finally {
            logger?.log(END + cmd)
        }
        return sb.toString()
    }

    private fun closeQuite(stream: Closeable) {
        try {
            stream.close()
        } catch (e: Exception) {
        }
    }
}

