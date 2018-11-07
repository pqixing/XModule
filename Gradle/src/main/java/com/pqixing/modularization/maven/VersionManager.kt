package com.pqixing.modularization.maven

import java.util.*

object VersionManager {

    private val versions = HashMap<String, String>()
    private var hasInit = false

    fun getVersion(module: String, branch: String): String {
        checkInit()
        return ""
    }

    private fun checkInit() {
        if (hasInit) return
        collectVersions()
        hasInit = true
    }

    private fun collectVersions() {
    }
}
