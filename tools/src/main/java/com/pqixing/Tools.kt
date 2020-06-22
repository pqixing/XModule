package com.pqixing

import com.pqixing.interfaces.ILog
import kotlin.system.exitProcess


object Tools  {
    var log = true
    fun println(l: String?) {
        if(log) logger?.println(l)?:kotlin.io.println(l)

    }
    fun printError(exitCode: Int, l: String?) {
        logger?.printError(exitCode,l)?: kotlin.run {
            if(log) kotlin.io.println(l)
            exitProcess(exitCode)
        }
    }
    fun println(exitCode: Int, l: String?) = if (exitCode < 0) printError(exitCode, l) else println(l)

    var logger: ILog? = null
}

fun String.getEnvValue(): String? = try {
    System.getProperty(this)
} catch (e: Exception) {
    null
}