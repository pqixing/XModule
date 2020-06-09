package com.pqixing.modularization.manager

import com.pqixing.Templet
import com.pqixing.modularization.base.IPlugin
import com.pqixing.modularization.manager.ExceptionManager.EXCEPTION_SYNC
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

/**
 * 管理文件的输出和读取
 */
object FileManager {


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
        if (clone == null) ExceptionManager.thow(EXCEPTION_SYNC, "can not clone doc project!!")

        //move doc to root dir
//        FileUtils.moveDir(tempDoc, plugin.rootDir)
//        FileUtils.delete(tempDoc)
        return clone!!
    }

}
