package com.pqixing.modularization.setting

import com.pqixing.Config
import com.pqixing.model.Module
import com.pqixing.model.ManifestModel
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import java.io.File

class ArgsExtends(val config: Config, val env: EnvArgs, val manifest: ManifestModel) {

    var versions: VersionManager = VersionManager(this)
    var runTaskNames = mutableListOf<String>()

    fun runAsApp(module: Module) = module.isApplication || runTaskNames.contains(":${module.name}:BuildApk") || runTaskNames.find { it.matches(Regex(":${module.name}:assemble.*?Dev")) } != null

    fun getPsw(value: String): String = GitUtils.getPsw(value)

    /**
     * 释放内部饮用
     */
    fun clear() {
        versions.pomCache.clear()
        versions = VersionManager(this)
        runTaskNames.clear()
    }
}

class EnvArgs(val rootDir: File, val config: Config, val gradleCache: File) {
    /**
     *
     */
    var basicBranch: String = "master"

    var basicDir: File = File(rootDir, FileNames.BASIC)
    var versionDir: File = File(gradleCache, "${FileNames.MODULARIZATION}/version")
    var versionFile: File = File(versionDir, "download.zip")
    var uploadFile: File = File(versionDir, "upload.txt")

    var codeRootDir: File = File(File(rootDir, config.codeRoot).canonicalPath)


}

