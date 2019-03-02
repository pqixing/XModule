package com.pqixing

import com.pqixing.interfaces.ILog


object Tools : ILog {
    override fun println(l: String?) {
        if (logger != this) logger.println(l)
        else {
            System.out.println(l)
        }
    }

    override fun printError(exitCode: Int, l: String?) {
        if (logger != this) logger.printError(exitCode, l)
        else {
            System.out.println(l)
            System.exit(exitCode)
        }
    }

    var logger: ILog = this
}