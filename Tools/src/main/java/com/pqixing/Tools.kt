package com.pqixing

import com.pqixing.git.GitUtils
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import com.pqixing.tools.FileUtils


object Tools : ILog {
    override fun printError(l: String?) = logger?.printError(l)

    override fun println(l: String?) = logger?.println(l)

    lateinit var rootDir: String
    lateinit var logger: ILog
    var init = false
    @JvmStatic
    fun init(logger: ILog, rootDir: String, credentials: ICredential) {
        Tools.rootDir = rootDir
        Tools.logger = logger
        GitUtils.init(credentials)
        FileUtils.init(Tools::class.java)
        init = true
    }
}