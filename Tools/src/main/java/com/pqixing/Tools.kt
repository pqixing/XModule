package com.pqixing

import com.pqixing.git.GitUtils
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog


object Tools {
    lateinit var rootDir: String
    lateinit var logger: ILog
    var init = false
    @JvmStatic
    fun init(logger: ILog, rootDir: String, credentials: ICredential) {
        Tools.rootDir = rootDir
        Tools.logger = logger
        GitUtils.init(credentials)
        init = true
    }
}