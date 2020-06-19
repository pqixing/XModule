package com.pqixing.modularization.setting

import com.pqixing.Config
import com.pqixing.help.MavenPom
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.Module
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import java.io.File
import java.util.*

class ArgsExtends(val config: Config, val env: EnvArgs, val projectXml: ProjectXmlModel) {

    var versions: VersionManager = VersionManager(this)
    val dpsContainer = mutableMapOf<String, DpsExtends>()
    var runTaskNames = mutableListOf<String>()

    fun runAsApp(module: Module) = module.isApplication || runTaskNames.contains(":${module.name}:BuildApk") || runTaskNames.find { it.matches(Regex(":${module.name}:assemble.*?Dev")) } != null
    fun runAsApp(name: String): Boolean {
        return runAsApp(projectXml.findModule(name) ?: return false)
    }

    fun getPsw(value: String): String = GitUtils.getPsw(value)

    /**
     * 释放内部饮用
     */
    fun clear() {
        versions = VersionManager(this)
        env.pomCache.clear()
        runTaskNames.clear()
        dpsContainer.clear()
    }
}

class EnvArgs(val rootDir: File, val config: Config) {
    /**
     *
     */
    var basicBranch: String = "master"

    var basicDir: File = File(rootDir, FileNames.BASIC)
    var dpsFile: File = File(basicDir, FileNames.DPS_MODULES)
    var xmlFile: File = File(basicDir, FileNames.PROJECT_XML)

    var codeRootDir: File = File(File(rootDir, config.codeRoot).canonicalPath)

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()
}
