package com.pqixing

import com.pqixing.interfaces.ILog
import com.pqixing.tools.FileUtils


object Tools {

    fun printError(l: String?,exitCode: Int = -1) = logger?.printError(exitCode,l)

    fun println(l: String?) = logger?.println(l)

    lateinit var rootDir: String
    lateinit var logger: ILog
    var init = false
    @JvmStatic
    fun init(logger: ILog, rootDir: String) {
        Tools.rootDir = rootDir
        Tools.logger = logger
        FileUtils.init(Tools::class.java)
        init = true
    }
}