package com.pqixing.modularization.setting

import com.pqixing.Config
import com.pqixing.EnvKeys
import com.pqixing.model.ManifestModel
import com.pqixing.model.Module
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import java.io.File

class ArgsExtends(val config: Config, val env: EnvArgs, val manifest: ManifestModel) {

    var vm: VersionManager = VersionManager(this)
    var runTaskNames = mutableListOf<String>()

    fun pxApp(module: Module) = module.type == Module.TYPE_APP || runTaskNames.contains(":${module.name}:BuildPxApk") || runTaskNames.find { it.matches(Regex(":${module.name}:assemble.*?Dev")) } != null

    fun getPsw(value: String): String = GitUtils.getPsw(value)

    /**
     * 释放内部饮用
     */
    fun clear() {
        vm.pomCache.clear()
        vm.loads.clear()
        vm = VersionManager(this)

        runTaskNames.clear()
    }
}

class EnvArgs(val rootDir: File, val config: Config, val gradleCache: File) {
    var basicDir: File = File(rootDir, EnvKeys.BASIC)
    var archivesFile: File = File(rootDir, "build/upload.txt")
    var codeRootDir: File = File(File(rootDir, config.codeRoot).canonicalPath)
}

