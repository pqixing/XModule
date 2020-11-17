package com.pqixing.modularization.setting

import com.pqixing.Config
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.base.XPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Logger
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.eclipse.jgit.api.Git
import java.io.File
import java.lang.ref.WeakReference

/**
 * 设置页面插件
 */
class ImportPlugin : Plugin<Settings> {
    companion object {

        fun Project.xPlugin() = findPlugin(this, XPlugin::class.java)!!
        fun Project.rootXPlugin() = this.rootProject.xPlugin()
        fun Project.allProject() = this.rootProject.allprojects

        fun Project.getArgs() = findArgs(this)
        fun Project.isRoot() = this == this.rootProject

        val settings = mutableMapOf<Int, WeakReference<ImportPlugin>>()

        fun getImport(project: Project) = settings[project.gradle.hashCode()]?.get()
        fun addPlugin(project: Project, plugin: Plugin<*>) {
            val import = getImport(project) ?: return
            val setOf = import.plugins[project] ?: mutableSetOf()
            setOf.add(plugin)
            import.plugins[project] = setOf
        }

        fun <T : Plugin<*>> findPlugin(project: Project, type: Class<T>): T? {
            val import = getImport(project) ?: return null
            return import.plugins[project]?.find { it.javaClass == type } as? T
        }

        fun findArgs(project: Project): ArgsExtends {
            return getImport(project)?.args!!
        }
    }

    var plugins = mutableMapOf<Project, MutableSet<Plugin<*>>>()
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
        if (!GitUtils.isGitDir(env.basicDir)) {
            val clone = downloadBasic(env.basicDir, setting, config) ?: return
            GitUtils.close(clone)
        }

        //解析project.xml，解析所有应用的依赖数据
        val projectXml = XmlHelper.loadManifest(rootDir.absolutePath)
        if (projectXml == null) {
            ResultUtils.thow("Miss manifest.xml on basic dir")
            return
        }
        args = ArgsExtends(config, env, projectXml)
        args.runTaskNames.addAll(taskNames)

        //解析include进行工程导入
        ImportScript(args, setting).startLoad()
        //在task开始前，执行任务的检查
        setting.gradle.afterProject { pro -> pro.tasks.mapNotNull { it as? BaseTask }.forEach { it.prepare() } }
        setting.gradle.taskGraph.whenReady { g -> g.allTasks.also { if (it.isNotEmpty()) Tools.println("TaskGraph -> ${it.joinToString(",") { i -> i.project.name + ":" + i.name }}") }.mapNotNull { it as? BaseTask }.forEach { it.whenReady() } }
        //覆盖gradle.properties
        FileUtils.readText(File(args.env.basicDir, "gradle.properties"))?.let {
            FileUtils.writeText(File(rootDir, "gradle.properties"), it)
        }

        setting.gradle.addListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                args.clear()
                settings.remove(key)
                plugins.clear()
            }
        })
    }

    private fun downloadBasic(basicDir: File, setting: Settings, config: Config): Git? {
        val urlName = "basicUrl"
        val url = config.basicUrl
        if (url?.isNotEmpty() != true) {//没有配置url
            ResultUtils.thow("Miss $urlName on gradle.properties,use $urlName=https://github.com/pqixing/md_basic.git for default")
            return null
        }
        //clone the dir
        val clone = GitUtils.clone(url, basicDir)
        if (clone == null) {
            ResultUtils.thow("Clone Fail : $url ,try to set user and password on Config.java")
        }
        return clone
    }
}
