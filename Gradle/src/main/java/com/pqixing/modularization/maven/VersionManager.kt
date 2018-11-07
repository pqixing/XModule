package com.pqixing.modularization.maven

import java.util.*

object VersionManager {

    private val versions = HashMap<String, String>()
    internal var hasInit = false

    fun getVersion(module: String, branch: String): String {
        return ""
    }

    fun checkInit() {
        if (hasInit) return
        collectVersions()
        hasInit = true
    }

    private fun collectVersions() {
    }
}
