package com.pqixing.modularization.base

import com.android.build.gradle.BaseExtension
import com.pqixing.EnvKeys
import com.pqixing.model.BrOpts
import com.pqixing.model.Module
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.DpsManager
import com.pqixing.modularization.android.PqxTransform
import com.pqixing.modularization.maven.IndexMavenTask
import com.pqixing.modularization.maven.MavenModel
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.modularization.maven.VersionParse
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.modularization.setting.ImportPlugin
import com.pqixing.modularization.setting.ImportPlugin.Companion.isRoot
import com.pqixing.modularization.setting.ImportPlugin.Companion.rootXPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.gradle.api.Project
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

open class XPlugin : BasePlugin() {

    lateinit var args: ArgsExtends

    var toMaven: MavenModel? = null

    override fun apply(p: Project) {
        super.apply(p)
        val doAfterList: MutableList<(p: Project) -> Unit> = mutableListOf()
        project.afterEvaluate { doAfterList.forEach { f -> f(it) } }

        doAfterList.add { it.tasks.findByName("clean")?.doLast { FileUtils.delete(File(project.projectDir, "build")) } }

        //添加maven仓库
        args = ImportPlugin.findArgs(project)

        val px = project.extensions.create("px", PXExtends::class.java)
        px.config = args.config
        px.manifest = args.manifest

        loadMaven(px)

        doAfterList.add { project.repositories.maven { it.url = px.maven.mavenUri() } }

        //查处当前project对应的模块信息
        val module = args.manifest.findModule(project.name)

        //生成忽略文件
        modifyIgnoreFile(module)

        if (module == null) return

        px.module = module

        //对于Andorid或者java工程，添加依赖管理
        if (module.forDps) {
            //加载依赖关系
            px.dpsManager = DpsManager(project, module)
            if (module.forMaven) BaseTask.task(project, ToMavenTask::class.java)

            //解析依赖
            doAfterList.add { project.apply(mapOf("from" to FileUtils.writeText(File(cacheDir, EnvKeys.GRADLE_DEPENDENCIES), px.dpsManager.resolveDps(), true))) }
        }

        //如果是Android工程，添加插件处理
        if (module.isAndroid) {
            val buildAsApp = args.pxApp(module)
            val isApp = module.type == Module.TYPE_APP

            //如果是空同步，不做任何处理
            //根据情况进行不同的Android插件依赖
            project.apply(mapOf<String, String>("plugin" to if (buildAsApp) Keys.NAME_APP else Keys.NAME_LIBRARY))

            val android: BaseExtension = project.extensions.getByName("android") as BaseExtension
            //如果是Library模块运行，设置ApplicationId
            if (buildAsApp && !isApp) {
                android.defaultConfig { it.applicationId = "com.${TextUtils.letter(module.branch(), "library")}.${TextUtils.letter(project.name, "app")}".toLowerCase() }
            }
            //开始注解切入
            doAfterList.add { if (buildAsApp && px.transform) android.registerTransform(PqxTransform()) }
        }
    }

    private fun loadMaven(px: PXExtends) {
        val manifest = px.manifest
        //初始化MavenInfo信息
        px.maven = MavenModel().also { maven ->
            maven.mavenUrl = manifest.mavenUrl
            maven.groupId = manifest.groupId
            maven.mavenUser = manifest.mavenUser
            maven.mavenPsw = manifest.mavenPsw
            maven.artifactId = project.name
        }

        if (project.isRoot()) {
            toMaven = loadRootMaven(px.maven)
            BaseTask.task(project, IndexMavenTask::class.java)
        }
        val rootX = project.rootXPlugin()
        rootX.toMaven?.takeIf { it.artifactId == project.name }?.let { m ->
            px.maven.groupId = m.groupId
            px.maven.version = m.version
            px.maven.lastRev = m.lastRev
            rootX.toMaven = null
        }
    }

    /**
     * 加载maven数据
     */
    private fun loadRootMaven(maven: MavenModel): MavenModel? {
        //如果执行的 IndexMavenTask
        if (BaseTask.matchTask(IndexMavenTask::class.java, args.runTaskNames)) {
            val brOpts = BrOpts(args.config.opts)
            maven.artifactId = EnvKeys.BASIC
            maven.version = VersionParse.getVersion(brOpts.target ?: EnvKeys.BASIC)
            maven.artifactFile = File(cacheDir, "${EnvKeys.BASIC}.txt")
            return null
        }

        //如果当前没有打包上传的设置
        if (!BaseTask.matchTask(ToMavenTask::class.java, args.runTaskNames)) return null

        //等待ToMaven的模块名称
        val module = args.runTaskNames.find { it.contains("ToMaven") }?.split(":ToMaven")?.firstOrNull()?.replace(":", "")?.let { args.manifest.findModule(it) }
                ?: return null

        //解析等待toMaven的工程的信息
        val open = GitUtils.open(File(args.env.codeRootDir, module.project.path)) ?: return null

        val artifactId = module.name
        val branch = open.repository.branch
        val groupId = VersionParse.getGroupId(args.manifest.groupId, branch)
        val baseVersion = module.version.takeIf { it != "+" } ?: args.manifest.initVersion

        val v = args.vm.getNewerVersion(branch, artifactId, baseVersion)
        val version = "$baseVersion.${v + 1}"

        val toMaven = MavenModel()
        toMaven.groupId = groupId
        toMaven.artifactId = artifactId
        toMaven.version = version
        toMaven.lastRev = loadGitInfo(open, module)
        GitUtils.close(open)

        //设置root工程的上传信息
        maven.groupId = "${args.manifest.groupId}.${EnvKeys.BASIC_LOG}"
        maven.artifactId = args.vm.lastVersion
        maven.artifactFile = File(cacheDir, "${args.vm.lastVersion}.zip")
        maven.version = "$groupId${EnvKeys.seperator}$artifactId${EnvKeys.seperator}$version"

        return toMaven
    }

    fun modifyIgnoreFile(module: Module?) {

        val ignoreFile = project.file(EnvKeys.GIT_IGNORE)

        val defSets = mutableSetOf("build", "*.iml")
        if (project.isRoot()) defSets.add(EnvKeys.USER_CONFIG)
        if (module?.type == Module.TYPE_LIB) defSets.add("src/dev")

        val old = FileUtils.readText(ignoreFile) ?: ""
        defSets -= old.lines().map { it.trim() }

        if (defSets.isEmpty()) return
        val txt = StringBuilder(old)
        defSets.forEach { txt.append("\n$it") }
        FileUtils.writeText(ignoreFile, txt.toString())
    }


    fun trimIgnoreKey(key: String): String {
        var start = 0
        var end = key.length
        if (key.startsWith("/")) start++
        if (key.endsWith("/")) end--
        return key.substring(start, end)
    }


    fun getRelativePath(path: String): String? {
        val of = path.indexOf("/")
        return if (of > 0) return path.substring(of + 1) else null
    }

    fun loadGitInfo(git: Git, module: Module): RevCommit? {
        val command = git.log().setMaxCount(1)
        getRelativePath(module.path)?.apply { command.addPath(this) }
        return command.call().find { true }
    }
}

