package com.pqixing.modularization.setting

import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.base.IPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Logger
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
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
        val config = XmlHelper.loadConfig(rootDir.absolutePath, setting.extensions.extraProperties.properties)
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
        val projectXml = XmlHelper.loadManifest(rootDir.absolutePath)
        if(projectXml==null){
            ResultUtils.thow("Miss manifest.xml on basic dir")
            return
        }
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
        val urlName = "${EnvKeys.BASIC}Url"
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
}