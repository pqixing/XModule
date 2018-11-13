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
    val docBranch = "MavenLog"

    lateinit var docCredentials: UsernamePasswordCredentialsProvider

    /**
     * 本地doc工程的信息
     */
    var docProject = GitProject()
    var codeRootDir: File? = null
        get() {
            if (field == null) {
                field = JGroovyHelper.getImpl(IExtHelper::class.java).getExtValue(BasePlugin.getPlugin(ManagerPlugin::class.java)?.project?.gradle, FileNames.CODE_ROOT) as File?
            }
            return field
        }

    /**
     * 存放信息的目录
     */
    var infoDir: File? = null
        get() {
            if (field == null) {
                field = File(docRoot, "ProjectInfo")
            }
            return field
        }

    var cacheRoot: File? = null
        get() {
            if (field == null) {
                field = File(BasePlugin.getPlugin(ManagerPlugin::class.java)?.cacheDir, FileNames.MODULARIZATION)
            }
            return field
        }
    var docRoot: File? = null
        get() {
            if (field == null) {
                field = File(BasePlugin.getPlugin(ManagerPlugin::class.java)?.rootDir, FileNames.DOCUMENT)
            }
            return field
        }

    fun getProjectXml(): File {
        return File(infoDir, FileNames.PROJECT_XML)
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
        val manager = getExtends(ManagerExtends::class.java)
        if (manager.docGitUrl.isEmpty()) {
            ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "doc git can not be empty!!")
        }
        val docRoot = docRoot!!
        if (docRoot.exists() && !GitUtils.isGitDir(docRoot)) {
            FileUtils.delete(docRoot)
        }

        val extends = plugin.getExtends(ManagerExtends::class.java)
        val info = plugin.projectInfo
        var user = extends.gitUserName
        var psw = extends.gitPassWord
        if (user.isEmpty()) user = info?.gitUserName ?: ""
        if (psw.isEmpty()) psw = info?.gitPassWord ?: ""

        docCredentials = UsernamePasswordCredentialsProvider(user, psw)

        val git = if (docRoot.exists()) {
            Git.open(docRoot).apply {
                //切换到log分支
                if (docBranch != repository.branch) {
                    stashCreate().init(docCredentials).execute()
                    stashDrop().init(docCredentials).execute()
                    checkout().setForce(true).setName(docBranch).init().execute()
                }
                pull().init(docCredentials).execute()
            }
        } else {

            Git.cloneRepository()
                    .setURI(manager.docGitUrl).setDirectory(docRoot).setBranch(docBranch)
                    .init(docCredentials).execute()
        }
        if (!docRoot.exists()) {
            ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "Clone doc fail !! Please check")
        }

        val filter = ProjectInfoFiles.files.filter { copyIfNull(it, docRoot) }
        //初始化dco目录的信息
        docProject.gitUrl = manager.docGitUrl
        docProject.name = FileNames.DOCUMENT
        docProject.rootName = FileNames.DOCUMENT
        docProject.introduce = "LogManager"
        if (git == null) return@with
        docProject.loadGitInfo(git)
        //如果有新增文件，提交
        if (filter.isNotEmpty()) {
            git.add().addFilepattern(".").init().execute()
            git.commit().setAllowEmpty(true).setMessage("add file $filter").init().execute()
            git.push().setForce(true).init().execute()
        }
        git.close()
    }

    /**
     * 如果工程目录下没有文件，拷贝
     */
    private fun copyIfNull(fileName: String, docRoot: File): Boolean {
        val f = File(docRoot, fileName)
        if (f.exists()) return false
        FileUtils.writeText(f, FileUtils.getTextFromResource(fileName))
        return true
    }
}
