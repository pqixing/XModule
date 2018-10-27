package com.pqixing

import com.pqixing.git.GitUtils
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import sun.security.krb5.Credentials


object Init {
    lateinit var rootDir: String
    lateinit var logger: ILog
    @JvmStatic
    fun init(logger: ILog, rootDir: String, credentials: ICredential) {
        Init.rootDir = rootDir
        Init.logger = logger
        GitUtils.init(credentials)
    }
}