package com.pqixing.modularization.manager

import com.pqixing.Templet
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.base.IPlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.manager.ExceptionManager.EXCEPTION_SYNC
import com.pqixing.modularization.manager.FileManager.rootDir
import com.pqixing.modularization.manager.FileManager.templetRoot
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import java.io.File

/**
 * 管理文件的输出和读取
 */
object FileManager : OnClear {
    override fun start() {

    }

    init {
        BasePlugin.addClearLister(this)
        start()
    }

    override fun clear() {
    }

    val rootDir
        get() = ManagerPlugin.getPlugin().rootDir
    val templetRoot: File
        get() = File(rootDir, "templet")

    fun getProjectXml()= File(templetRoot, FileNames.PROJECT_XML)

    /**
     * 检测需要导出的文件有没有被导出
     * 待检测项
     * ${cacheDir}/ImportProject.gradle  若不存在或有更新，替换文件
     * setting.gradle  若不包含指定代码，添加代码
     * include.kt   若不存在，生成模板
     * templet.groovy  若不存在，生成模板
     */
    fun checkFileExist(plugin: IPlugin) {
        val rootDir = plugin.rootDir
        Templet.setting.forEach { s ->
            val f = File(rootDir, s)
            if (!f.exists() || s.endsWith(".gradle")) FileUtils.writeText(f, FileUtils.getTextFromResource("setting/$s"), true)
        }

        with(File(plugin.rootDir, "." + Templet.gitignore)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource(Templet.gitignore))
        }
        val settingFile = File(plugin.rootDir, Templet.settings_gradle)
        val source = if (settingFile.exists()) settingFile.readText() else ""
        val replace = FileUtils.replaceOrInsert("//Auto Code Start", "//Auto Code End", FileUtils.getTextFromResource(Templet.settings_gradle), source)
        FileUtils.writeText(settingFile, replace, true)
    }


    /**
     * 检查本地Document目录
     * Document 目录用来存放一些公共的配置文件
     */
    fun checkDocument(plugin: ManagerPlugin) {
        val extends = ManagerPlugin.getExtends()
        val docGit = GitUtils.open(templetRoot) ?: createDocGit(plugin)
        extends.docRepoBranch = docGit.repository.branch

        var i = mutableListOf<String>()
        Templet.templet.map { "templet/$it" }.forEach { s ->
            val f = File(rootDir, s)
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
        trySyncFile(extends)
    }

    fun trySyncFile(extends: ManagerExtends) {
        if (extends.config.syncBuildFile&&GitUtils.isGitDir(templetRoot)) {
            FileUtils.writeText(File(rootDir, "build.gradle"), File(rootDir, "templet/build.gradle").readText(), true)
            FileUtils.writeText(File(rootDir, "gradle.properties"), File(rootDir, "templet/gradle.properties").readText(), true)
            FileUtils.writeText(File(rootDir, "gradle/wrapper/gradle-wrapper.properties"), File(rootDir, "templet/gradle-wrapper.properties").readText(), true)
        }
    }

    private fun createDocGit(plugin: ManagerPlugin): Git {
        val extends = ManagerPlugin.getExtends()
        val tempDoc = templetRoot
        if (tempDoc.exists()) {
            FileUtils.delete(tempDoc)
        }
        //clone the dir
        val clone = GitUtils.clone(extends.docRepoUrl, tempDoc)
        if (clone == null) ExceptionManager.thow(EXCEPTION_SYNC, "can not clone doc project!!")

        //move doc to root dir
//        FileUtils.moveDir(tempDoc, plugin.rootDir)
//        FileUtils.delete(tempDoc)
        return clone!!
    }

}
