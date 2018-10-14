package com.pqixing.tools

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Shell {
    private static Logger logger = null
    private static final String END = "Shell execute end..."
    private static final String START = "Shell execute start..."
    static final int STATUS_START = 0
    static final int STATUS_PROCESS = 1
    static final int STATUS_END = 2
    static final int STATUS_ERROR_END = 3

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue())

    static void run(String cmd, File dir, boolean sync = true, ShellCallBack callBack) {
        def process = cmd.execute([], dir)
        if (sync) handleResult(process, callBack)
        else pool.execute { handleResult(process, callBack) }
    }

    private static void handleResult(Process process, ShellCallBack callBack) {
        def resultCache = new LinkedBlockingQueue()
        def streamIn = process.inputStream.newReader()
        def streamErr = process.errorStream.newReader()
        def streamOut = process.outputStream.newWriter()

        resultCache.put(START)
        def readIn = new Runnable() {

            @Override
            void run() {
                String str
                while ((str = streamIn.readLine()) != null) {
                    resultCache.put(str)
                }
                logger?.log("readIn exit")
            }
        }

        def readErr = new Runnable() {

            @Override
            void run() {
                def reader = streamErr.newReader()
                String str
                while ((str = reader.readLine()) != null) {
                    resultCache.put(str)
                }
                logger?.log("readErr exit")
            }
        }

        def write = new Runnable() {

            @Override
            void run() {
                streamOut.write("echo '$END'")

                logger?.log("write exit")
            }
        }
        pool.execute(write)
        pool.execute(readIn)
        pool.execute(readErr)

        String line
        while (true) {
            line = resultCache.poll(30, TimeUnit.SECONDS)
            if (line == null) {
                callBack.call(STATUS_ERROR_END, "")
                break
            } else if (line == START) {
                callBack.call(STATUS_START, line)
            } else if (line == END) {
                callBack.call(STATUS_END, line)
                break
            } else {
                callBack.call(STATUS_END, line)
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
    }

    private static void closeQuite(Closeable stream) {
        try {
            stream.close()
        } catch (Exception e) {
        }
    }
}

