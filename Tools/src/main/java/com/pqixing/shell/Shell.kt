package com.pqixing.shell

import com.pqixing.Tools
import java.io.Closeable
import java.io.File
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Shell {
    private val END = "-------- END SHELL -> "
    private val START = "-------- START SHELL -> "

    private val pool = ThreadPoolExecutor(2, 2, 10L, TimeUnit.SECONDS, LinkedBlockingQueue())


    @JvmStatic
    fun runSync(cmd: List<String>) {
        cmd.forEach { runSync(it, null) }
    }

    @JvmStatic
    fun runSync(cmd: String): LinkedList<String> {
        return runSync(cmd, null)
    }

    @JvmStatic
    fun runSync(cmd: String, dir: File? = null): LinkedList<String> {
        Tools.logger?.println(START + cmd)
        val r = LinkedList<String>()
        cmd.split("&").forEach {
            if (it.isNotEmpty()) {
                val c = it.trim()
                try {
                    val process = Runtime.getRuntime().exec(c, arrayOf(), dir)
                    r += handleResult(process)
                } catch (e: Exception) {
                    Tools.logger?.println(e.toString())
                }
            }
        }
        return r
    }

    fun runASync(cmd: String, dir: File? = null) = pool.execute {
        runSync(cmd, dir)
    }

    private fun handleResult(process: Process): LinkedList<String> {
        val resultCache = LinkedBlockingQueue<String>()
        val streamIn = process.inputStream.bufferedReader()
        val streamErr = process.errorStream.bufferedReader()
        val afterRun = arrayOf(false, false)

        Thread {
            var str: String? = null
            do {
                try {
                    str = streamIn.readLine()
                } catch (e: Exception) {
                    Tools.logger.println(e.toString())
                }
                if (str != null) resultCache.put(str)
            } while (str != null)
            afterRun[0] = true
        }.start()
        Thread {
            var str: String? = null
            do {
                try {
                    str = streamErr.readLine()
                } catch (e: Exception) {
                    Tools.logger.println(e.toString())
                }
                if (str != null) resultCache.put(str)
            } while (str != null)
            afterRun[1] = true
        }.start()


        var line: String?
        val result = LinkedList<String>()
        var lastLineTime = System.currentTimeMillis()
        while (true) {
            line = resultCache.poll()
            if (line == null) {
                if (afterRun[0] && afterRun[1]) {
                    break
                }
                if (System.currentTimeMillis() - lastLineTime > 1000 * 15) {
                    Tools.logger?.println("process exit by time out")
                }
                continue
            } else {
                lastLineTime = System.currentTimeMillis()
                Tools.logger?.println(line)
                result += line
            }
        }

        closeQuite(streamIn)
        closeQuite(streamErr)
        try {
            process.destroy()
        } finally {
        }
        return result
    }

    private fun closeQuite(stream: Closeable) {
        try {
            stream.close()
        } catch (e: Exception) {
        }
    }
}

