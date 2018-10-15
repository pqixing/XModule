package com.pqixing

import java.util.logging.Logger

object Init {
    lateinit var rootDir: String
    lateinit var logger: Logger
    @JvmStatic
    fun init(logger: Logger, rootDir: String) {
        Init.rootDir = rootDir
        Init.logger = logger
    }
}