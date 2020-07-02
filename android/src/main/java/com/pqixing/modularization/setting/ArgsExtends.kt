package com.pqixing.modularization.setting

import com.pqixing.Config
import com.pqixing.help.MavenPom
import com.pqixing.model.Module
import com.pqixing.model.ManifestModel
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import java.io.File
import java.util.*

class ArgsExtends(val config: Config, val env: EnvArgs, val manifest: ManifestModel) {

    var versions: VersionManager = VersionManager(this)
    var runTaskNames = mutableListOf<String>()

    fun runAsApp(module: Module) = module.isApplication || runTaskNames.contains(":${module.name}:BuildApk") || runTaskNames.find { it.matches(Regex(":${module.name}:assemble.*?Dev")) } != null

    fun getPsw(value: String): String = GitUtils.getPsw(value)

    /**
     * 释放内部饮用
     */
    fun clear() {
        versions = VersionManager(this)
        env.pomCache.clear()
        runTaskNames.clear()
    }
}

class EnvArgs(val rootDir: File, val config: Config) {
    /**
     *
     */
    var basicBranch: String = "master"

    var basicDir: File = File(rootDir, FileNames.BASIC)
    var versionDir: File = File(rootDir, "build/versions")
    var versionFile: File = File(versionDir, "version.properties")
    var versionUpFile: File = File(versionDir, "up.properties")
    var xmlFile: File = File(basicDir, FileNames.PROJECT_XML)

    var codeRootDir: File = File(File(rootDir, config.codeRoot).canonicalPath)

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()
}

