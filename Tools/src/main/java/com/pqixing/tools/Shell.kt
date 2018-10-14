package com.pqixing.tools

import java.io.Closeable
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Shell {
    private var logger: Logger? = null
    private val END = "Shell execute end..."
    private val START = "Shell execute start..."
    val STATUS_START = 0
    val STATUS_PROCESS = 1
    val STATUS_END = 2
    val STATUS_ERROR_END = 3

    private val pool = ThreadPoolExecutor(4, 4, 30L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @JvmStatic
    fun testRun(cm:String){
        logger?.log(cm)
    }
    @JvmStatic
    fun run(cmd: String, dir: File, sync: Boolean = true, callBack: ShellCallBack? = null) {
        val process = Runtime.getRuntime().exec(cmd, arrayOf(), dir)

        if (sync) handleResult(cmd, process, callBack)
        else pool.execute { handleResult(cmd, process, callBack) }
    }

    private fun handleResult(cmd: String, process: Process, callBack: ShellCallBack?): String {
        logger?.log("handleResult start")
        val resultCache = LinkedBlockingQueue<String>()
        val streamIn = process.inputStream.bufferedReader()
        val streamErr = process.errorStream.bufferedReader()
        val streamOut = process.outputStream.bufferedWriter()

        resultCache.put(START)
        val readIn = Runnable {

            var str: String? = null
            do {
                str = streamIn.readLine()
                resultCache.put(str)
            } while (str != null)
            logger?.log("readIn exit")
        }
        val readErr = Runnable {

            var str: String? = null
            do {
                str = streamErr.readLine()
                resultCache.put(str)
            } while (str != null)
            logger?.log("readErr exit")
        }
        val write = Runnable {

            streamOut.write("echo '$END'")
            logger?.log("write exit")
        }


        pool.execute(write)
        pool.execute(readIn)
        pool.execute(readErr)

        var line: String?
        val sb = StringBuilder()
        while (true) {
            line = resultCache.poll(30, TimeUnit.SECONDS)
            if (line == null) {
                callBack?.call(STATUS_ERROR_END, "ERROR")
                break
            } else if (line == START) {
                callBack?.call(STATUS_START, START + cmd)
            } else if (line == END) {
                callBack?.call(STATUS_END, END + cmd)
                break
            } else {
                callBack?.call(STATUS_PROCESS, line)
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(line)
                logger?.log(line)
            }
        }

        closeQuite(streamIn)
        closeQuite(streamErr)
        closeQuite(streamOut)
        try {
            process.destroy()
        } finally {
            logger?.log("handleResult exit")
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

