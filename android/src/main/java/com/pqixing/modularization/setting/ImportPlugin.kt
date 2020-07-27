package com.pqixing.modularization.setting

import com.alibaba.fastjson.JSON
import com.pqixing.Config
import com.pqixing.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.base.IPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Logger
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import org.eclipse.jgit.api.Git
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtraPropertiesExtension
import java.io.File
import java.lang.ref.WeakReference

/**
 * 设置页面插件
 */
class ImportPlugin : Plugin<Settings> {
    companion object {
        val settings = mutableMapOf<Int, WeakReference<ImportPlugin>>()
        fun addPlugin(project: Project, plugin: IPlugin) {
            settings[project.gradle.hashCode()]?.get()?.plugins?.put(project, plugin)
        }

        fun findPlugin(project: Project): IPlugin? {
            return settings[project.gradle.hashCode()]?.get()?.plugins?.get(project)
        }

        fun findArgs(project: Project): ArgsExtends {
            return settings[project.gradle.hashCode()]?.get()?.args!!
        }
    }

    var plugins = mutableMapOf<Project, IPlugin>()
    lateinit var args: ArgsExtends
    override fun apply(setting: Settings) {
        Tools.logger = Logger()
        Tools.println("> Configure project :${setting.rootProject.name}")
        val start = System.currentTimeMillis()
        val key = setting.gradle.hashCode()
        settings[key] = WeakReference(this)

        val rootDir = setting.rootDir

        //检查配置文件Config,读取config和env环境变量，生成全局配置
        val config = loadConfig(rootDir, setting.extensions.extraProperties)
        Tools.log = config.log
        GitUtils.gitPsw = config.userName
        GitUtils.gitUser = GitUtils.getPsw(config.passWord)

        val env = EnvArgs(rootDir, config, setting.gradle.gradleUserHomeDir)
        val taskNames = setting.gradle.startParameter.taskNames

        //检查basic是否存在，如果不存在，尝试读取gradle.properties进行下载，否则抛出异常
        val basicGit = GitUtils.open(env.basicDir) ?: downloadBasic(env.basicDir, setting) ?: return
        env.basicBranch = basicGit.repository.branch
        if (taskNames.find { it.contains(":ToMaven") } != null) GitUtils.pull(basicGit)
        GitUtils.close(basicGit)

        //解析project.xml，解析所有应用的依赖数据
        val projectXml = XmlHelper.parseManifest(env.xmlFile)
        args = ArgsExtends(config, env, projectXml)
        args.runTaskNames.addAll(taskNames)

        //解析include进行工程导入
        ImportScript(args, setting).startLoad()
        //在task开始前，执行任务的检查
        setting.gradle.afterProject { pro -> pro.tasks.mapNotNull { it as? BaseTask }.forEach { it.prepare() } }
        setting.gradle.taskGraph.whenReady { g -> g.allTasks.also { Tools.println("TaskGraph -> ${it.joinToString(",") { i -> i.project.name + ":" + i.name }}") }.mapNotNull { it as? BaseTask }.forEach { it.whenReady() } }
        //覆盖gradle.properties
        FileUtils.readText(File(args.env.basicDir, "gradle.properties"))?.let {
            FileUtils.writeText(File(rootDir, "gradle.properties"), it)
        }

        setting.gradle.addListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                args.clear()
                settings.remove(key)
                plugins.clear()
                ResultUtils.notifyIde(rootDir, mutableMapOf("task" to taskNames.joinToString(",")
                        , "type" to "buildFinished"
                        , "spend" to (System.currentTimeMillis() - start).toString()))
            }
        })
    }

    private fun downloadBasic(basicDir: File, setting: Settings): Git? {
        val urlName = "${FileNames.BASIC}Url"
        val url = kotlin.runCatching { setting.extensions.extraProperties.get(urlName).toString() }.getOrNull()
        if (url?.isNotEmpty() != true) {//没有配置url
            ResultUtils.notifyIde(setting.rootDir, mutableMapOf("type" to "Miss_${urlName}"))
            ResultUtils.thow("Miss $urlName on gradle.properties,use $urlName=https://github.com/pqixing/md_basic.git for default")
            return null
        }
        //clone the dir
        val clone = GitUtils.clone(url, basicDir)
        if (clone == null) {
            ResultUtils.notifyIde(setting.rootDir, mutableMapOf("type" to "Miss_${urlName}"))
            ResultUtils.thow("Clone Fail : $url ,try to set user and password on Config.java")
        }
        return clone
    }

    private fun loadConfig(rootDir: File, extras: ExtraPropertiesExtension): Config {
        val configFile = File(rootDir, FileNames.USER_CONFIG)
        if (!configFile.exists()) FileUtils.writeText(configFile, FileUtils.fromRes(configFile.name))

        val config = try {
            val parseClass = GroovyClassLoader().parseClass(configFile)
            JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), Config::class.java)
        } catch (e: Exception) {
            Config()
        }
        /**
         * 从系统配置中加载对应的变量
         */
        config.javaClass.fields.forEach {
            val key = it.name
            it.isAccessible = true

            //从ext或者gradle.properties中读取配置信息
            kotlin.runCatching {
                val extValue = extras.get(key)?.toString()
                if (extValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, extValue.toBoolean())
                    String::class.java -> it.set(config, extValue)
                }
            }

            //从传入的环境中读取配置信息
            kotlin.runCatching {
                val envValue = TextUtils.getSystemEnv(key)
                if (envValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, envValue.toBoolean())
                    String::class.java -> it.set(config, envValue)
                }
            }
        }
        return config
    }
}