package com.pqixing.modularization.manager

import com.pqixing.modularization.FileNames
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.ProjectInfoFiles
import com.pqixing.git.*
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.base.IPlugin
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * 管理文件的输出和读取
 */
object FileManager {

    lateinit var docCredentials: UsernamePasswordCredentialsProvider

    /**
     * 本地doc工程的信息
     */
    lateinit var docProject: Components
    var codeRootDir: File? = null
        get() {
            if (field == null) {
                field = JGroovyHelper.getImpl(IExtHelper::class.java).getExtValue(ManagerPlugin.getManagerPlugin().project.gradle, FileNames.CODE_ROOT) as File?
            }
            return field
        }

    var cacheRoot: File? = null
        get() {
            if (field == null) {
                field = File(ManagerPlugin.getManagerPlugin().cacheDir, FileNames.MODULARIZATION)
            }
            return field
        }
    var docRoot: File? = null
        get() {
            if (field == null) {
                field = File(ManagerPlugin.getManagerPlugin().rootDir, FileNames.MANAGER)
            }
            return field
        }

    fun getProjectXml(): File {
        return File(docRoot, FileNames.PROJECT_XML)
    }

    /**
     * 检测需要导出的文件有没有被导出
     * 待检测项
     * ${cacheDir}/ImportProject.gradle  若不存在或有更新，替换文件
     * setting.gradle  若不包含指定代码，添加代码
     * include.kt   若不存在，生成模板
     * ProjectInfo.groovy  若不存在，生成模板
     */
    fun checkFileExist(plugin: IPlugin): String {
        var error = ""
        with(File(plugin.rootDir, FileNames.IMPORT_KT)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/import.kt"))
        }
        with(File(plugin.rootDir, FileNames.PROJECT_INFO)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/ProjectInfo.java"))
        }

        with(File(plugin.rootDir, FileNames.SETTINGS_GRADLE)) {
            var e = "setting change"
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/settings.gradle"))
            else if (readText().lines().find { it.trim().endsWith("//END") } == null) {
                appendText(FileUtils.getTextFromResource("setting/settings.gradle"))
            } else {
                e = ""
            }
            error += e
            Unit
        }
        with(File(plugin.cacheDir, FileNames.IMPORTPROJECT_GRADLE)) {
            val importProject = FileUtils.getTextFromResource("setting/${FileNames.IMPORTPROJECT_GRADLE}")
            FileUtils.writeText(this, importProject, true)
//            error += "ImportProject.gradle has update!! try sync again"
        }
        return error
    }


    /**
     * 检查本地Document目录
     * Document 目录用来存放一些公共的配置文件
     */
    fun checkDocument(plugin: IPlugin) = with(plugin) {
        val docRoot = docRoot!!

        val extends = plugin.getExtends(ManagerExtends::class.java)
        val info = plugin.projectInfo
        var user = extends.gitUserName
        var psw = extends.gitPassWord
        if (user.isEmpty()) user = info?.gitUserName ?: ""
        if (psw.isEmpty()) psw = info?.gitPassWord ?: ""

        docCredentials = UsernamePasswordCredentialsProvider(user, psw)

        val git = Git.open(plugin.projectDir).apply {
            pull().init(docCredentials).execute()
        }

        val filter = ProjectInfoFiles.files.filter { copyIfNull(it, docRoot) }
        //初始化dco目录的信息
        docProject = Components(FileNames.ROOT, "", "LogManager", FileNames.MANAGER, Components.TYPE_DOCUMENT)
        if (git == null) return@with
        docProject.loadGitInfo(git)

        if(extends.branch.isEmpty()){
            extends.branch = docProject.lastLog.branch
        }
        ProjectManager.rootBranch = extends.branch
        //如果有新增文件，提交
        if (filter.isNotEmpty()) {
            git.add().addFilepattern(".").init(docCredentials).execute()
            git.commit().setAllowEmpty(true).setMessage("add file $filter").init(docCredentials).execute()
            git.push().setForce(true).init(docCredentials).execute()
        }
        git.close()
    }

    /**
     * 如果工程目录下没有文件，拷贝
     */
    private fun copyIfNull(fileName: String, docRoot: File): Boolean {
        val outFile = fileName.replace("ProjectInfo/", "")
        val f = File(docRoot, outFile)
        if (f.exists()) return false
        FileUtils.writeText(f, FileUtils.getTextFromResource(fileName))
        return true
    }
}
