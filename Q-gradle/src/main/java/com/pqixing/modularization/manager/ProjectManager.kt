package com.pqixing.modularization.manager

import com.pqixing.Templet
import com.pqixing.Tools
import com.pqixing.model.SubModule
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.*

object ProjectManager {
    var EXCEPTION_SYNC = 0
    var EXCEPTION_TASK = 1
    var EXCEPTION_PROJECT = 2

    fun thow(type: Int, error: String?) {
        throw GradleException(error)
    }

    fun tryCheckProject(project: Project,target:String):SubModule?{
        return checkProject(project.rootProject.allprojects.find { it.name == target } ?: return null)
    }

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project): SubModule? {
        val args = project.getArgs()
        //不在配置文件的git工程，不进行管理
        val subModule = args.projectXml.findSubModuleByName(project.name) ?: return null
        if (subModule.hasCheck) return subModule

        val info = args.config
        val buildDir = info.buildDir.toString().trim()
        //重新设置build 目录
        project.buildDir = File(project.projectDir, "build/" + (if (buildDir.isEmpty()) "default" else buildDir))

        val projectDir = File(args.env.codeRootDir, subModule.project.path)

        val docRepoBranch = args.env.templetBranch
        var mBranch = subModule.project.branch
        Tools.println("Check::${project.name}  $projectDir")
        if (mBranch.isEmpty()) if (GitUtils.isGitDir(projectDir)) Git.open(projectDir)
        else {
            FileUtils.delete(projectDir)
            GitUtils.clone(subModule.project.url, projectDir, docRepoBranch)
        }?.apply {
            mBranch = this.repository.branch
            subModule.project.branch = mBranch
            GitUtils.close(this)
        }
        if (mBranch != docRepoBranch) {
            Tools.println("   Warming::branch diff $mBranch -> $docRepoBranch")
        }
        subModule.hasCheck = true
        //如果build文件存在，不重新生成代码
        val buildGradle = File(args.env.codeRootDir, subModule.path + "/build.gradle")
        if (buildGradle.exists()) return subModule

        FileUtils.writeText(buildGradle, "apply plugin: 'com.module.android' ")

        Tools.println("   Create src ${buildGradle.absolutePath}")
        val groupName = args.projectXml.mavenGroup
        //如果是Api工程,检查基础模块在不在
        if (subModule.hasAttach()) {
            val moduleDir = buildGradle.parentFile

            val name = TextUtils.numOrLetter(subModule.name.substringBeforeLast("_"))
            val packageName = "${groupName.replace(".", "/")}/auto/router/${name}api"
            val className = "${TextUtils.firstUp(name)}Api"
            FileUtils.writeText(File(moduleDir, "java/$packageName/$className.java").takeIf { !it.exists() }
                    , "package ${packageName.replace("/", ".")};\nfinal class $className {}")

            FileUtils.writeText(File(moduleDir, "resources/values/strings.xml").takeIf { !it.exists() }, "<resources></resources>")


            val emptyManifest = FileUtils.readText(File(args.env.templetRoot, "android/Empty_AndroidManifest.xml"))!!
                    .replace("[groupName]", groupName).replace("[projectName]", subModule.name)

            //写入空清单文件
            FileUtils.writeText(File(moduleDir, "AndroidManifest.xml").takeIf { !it.exists() }, emptyManifest)

        } else if (args.projectXml.createSrc) {
            val moduleDir = File(buildGradle.parentFile,"src/main")

            val className = TextUtils.firstUp(TextUtils.numOrLetter(subModule.name))
            val packageName = groupName.replace(".", "/")+"/"+className.toLowerCase(Locale.CHINA)
            FileUtils.writeText(File(moduleDir, "java/$packageName/${className}App.java").takeIf { !it.exists() }
                    , "package ${packageName.replace("/", ".")};\nfinal class ${className}App {}")

            FileUtils.writeText(File(moduleDir, "resources/values/strings.xml").takeIf { !it.exists() }, "<resources></resources>")

            val emptyManifest = FileUtils.readText(File(args.env.templetRoot, "android/Empty_AndroidManifest.xml"))!!
                    .replace("[groupName]", groupName).replace("[projectName]", subModule.name)

            //写入空清单文件
            FileUtils.writeText(File(moduleDir, "AndroidManifest.xml").takeIf { !it.exists() }, emptyManifest)
        }

        return subModule
    }



    /**
     * 检测需要导出的文件有没有被导出
     * 待检测项
     * ${cacheDir}/ImportProject.gradle  若不存在或有更新，替换文件
     * setting.gradle  若不包含指定代码，添加代码
     * include.kt   若不存在，生成模板
     * templet.groovy  若不存在，生成模板
     */
    fun checkFileExist(project: Project) {
        val rootDir = project.rootDir
        Templet.setting.forEach { s ->
            val f = File(rootDir, s)
            if (!f.exists() || s.endsWith(".gradle")) FileUtils.writeText(f, FileUtils.getTextFromResource("setting/$s"), true)
        }

        with(File(rootDir, "." + Templet.gitignore)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource(Templet.gitignore))
        }
        val settingFile = File(rootDir, Templet.settings_gradle)
        val source = if (settingFile.exists()) settingFile.readText() else ""
        val replace = FileUtils.replaceOrInsert("//Auto Code Start", "//Auto Code End", FileUtils.getTextFromResource(Templet.settings_gradle), source)
        FileUtils.writeText(settingFile, replace, true)
    }


    /**
     * 检查本地Document目录
     * Document 目录用来存放一些公共的配置文件
     */
    fun checkDocument(plugin: ManagerPlugin) {
        val args = plugin.args
        val docGit = GitUtils.open(args.env.templetRoot) ?: createDocGit(plugin)
        args.env.templetBranch = docGit.repository.branch

        var i = mutableListOf<String>()
        Templet.templet.map { "templet/$it" }.forEach { s ->
            val f = File(args.env.rootDir, s)
            if (!f.exists()) {
                i.add(s)
                com.pqixing.tools.FileUtils.writeText(f, com.pqixing.tools.FileUtils.getTextFromResource(s), true)
            }
        }
//        if (i.isNotEmpty()) {
//            GitUtils.addAndPush(docGit, ".", "add file $i", true)
//        }
        GitUtils.close(docGit)
        //更新编译相关文件
        trySyncFile(args)
    }

    fun trySyncFile(anExtends: ArgsExtends) {
        val rootDir = anExtends.env.rootDir
        if (anExtends.config.syncBuildFile && GitUtils.isGitDir(anExtends.env.templetRoot)) {
            FileUtils.writeText(File(rootDir, "build.gradle"), File(rootDir, "templet/build.gradle").readText(), true)
            FileUtils.writeText(File(rootDir, "gradle.properties"), File(rootDir, "templet/gradle.properties").readText(), true)
            FileUtils.writeText(File(rootDir, "gradle/wrapper/gradle-wrapper.properties"), File(rootDir, "templet/gradles/wrapper/gradle-wrapper.properties").readText(), true)
        }
    }

    private fun createDocGit(plugin: ManagerPlugin): Git {
        val args = plugin.args
        val tempDoc = args.env.templetRoot
        if (tempDoc.exists()) {
            FileUtils.delete(tempDoc)
        }
        //clone the dir
        val clone = GitUtils.clone(args.projectXml.templetUrl, tempDoc)
        if (clone == null) thow(EXCEPTION_SYNC, "can not clone doc project!!")

        //move doc to root dir
//        FileUtils.moveDir(tempDoc, plugin.rootDir)
//        FileUtils.delete(tempDoc)
        return clone!!
    }
}
