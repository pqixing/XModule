package com.pqixing.modularization.manager

import com.alibaba.fastjson.JSON
import com.pqixing.Config
import com.pqixing.Tools
import com.pqixing.help.MavenPom
import com.pqixing.help.XmlHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import org.gradle.api.Project
import java.io.File
import java.util.*

class ArgsExtends {

    lateinit var project: Project
    lateinit var config: Config
    lateinit var projectXml: ProjectXmlModel
    lateinit var versions: VersionManager
    lateinit var env: EnvArgs
    var runTaskNames = mutableListOf<String>()

    fun getPsw(value: String): String {
        if (value.startsWith("sk:")) return ResultUtils.base64Decode(value.substring(3))
        Tools.println("Warming::password suggest $value -> ${"sk:" + ResultUtils.base64Encode(value)}")
        return value
    }

    fun load(project: Project): ArgsExtends {
        this.project = project
        runTaskNames.addAll(project.gradle.startParameter.taskNames)
        env = EnvArgs(project)
        config = loadConfig()
        projectXml = XmlHelper.parseProjectXml(env.xmlFile)

        env.load(this)

        versions = VersionManager(this)
        GitUtils.gitPsw = config.userName
        GitUtils.gitUser = getPsw(config.passWord)

        return this
    }

    /**
     * 释放内部饮用
     */
    fun clear() {
        versions = VersionManager(this)
        env.pomCache.clear()
        env = EnvArgs(project)
        projectXml = ProjectXmlModel("")
        runTaskNames.clear()
    }

    private fun loadConfig(): Config {
        val config = try {
            val parseClass = GroovyClassLoader().parseClass(File(env.rootDir, FileNames.USER_CONFIG))
            JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), Config::class.java)
        } catch (e: Exception) {
            Config()
        }

        /**
         * 从系统配置中加载对应的变量
         */
        config.javaClass.fields.forEach {
            val value = TextUtils.getSystemEnv(it.name) ?: return@forEach
            try {
                it.isAccessible = true
                when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, value.toBoolean())
                    String::class.java -> it.set(config, value)
                }
            } catch (e: Exception) {
                Tools.println("loadProjectInfo Exception -> $e")
            }
        }
        return config
    }

}

class EnvArgs(val project: Project) {

    /**
     *
     */
    var templetBranch: String = "master"

    var rootDir: File = project.rootDir
    var templetRoot: File = File(rootDir, "templet")
    var xmlFile: File = File(templetRoot, FileNames.PROJECT_XML)

    lateinit var codeRootDir: File

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()

    fun load(args: ArgsExtends) {
        codeRootDir = File(rootDir, args.config.codeRoot)

    }
}

