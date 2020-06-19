package com.pqixing.modularization.utils

import com.pqixing.interfaces.ILog

class Logger : ILog {
    override fun printError(exitCode: Int, l: String?) = ResultUtils.writeResult(l
            ?: "", exitCode, true)

    override fun println(l: String?) = System.out.println(l)
}