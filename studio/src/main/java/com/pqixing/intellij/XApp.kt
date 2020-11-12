package com.pqixing.intellij

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.model.ProjectModel
import com.pqixing.tools.PropertiesUtils
import git4idea.GitUtil
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


object XApp {

    val maps: Properties by lazy { PropertiesUtils.readProperties(File(cacheDir(), "share.xml")) }
    val executors: ScheduledExecutorService by lazy { Executors.newScheduledThreadPool(1) }
    private val LOG_EVENT: NotificationGroup by lazy { NotificationGroup.logOnlyGroup("XApp") }
    private val LOG: Logger by lazy { Logger.getInstance(XApp::class.java) }
    private val basicProject = mutableMapOf<String, Boolean>()

    fun key(project: Project?) = project?.basePath?.hashCode()?.toString() ?: "null"
    fun String.toKey(project: Project?) = "${key(project)}_$this"

    fun isBasic(project: Project?, update: Boolean = false): Boolean {
        val basePath = project?.basePath ?: return false
        var exist = basicProject[basePath]
        if (update || exist == null) {
            exist = File(project?.basePath, EnvKeys.XML_MANIFEST).exists()
            basicProject[basePath] = exist
        }
        return exist
    }

    fun post(delay: Long = 0L, cmd: () -> Unit) = executors.schedule(cmd, delay, TimeUnit.MILLISECONDS)

    @JvmStatic
    fun post(delay: Long, cmd: Runnable) = executors.schedule(cmd, delay, TimeUnit.MILLISECONDS)

    fun ideaApp() = ApplicationManager.getApplication()

    fun runAsyn(project: Project? = null, title: String = "Start Task ", cmd: (indicator: ProgressIndicator) -> Unit) {

        val importTask = object : Task.Backgroundable(project, title) {
            override fun run(indicator: ProgressIndicator) {
                val start = System.currentTimeMillis()
                println("runAsyn start -> $title")
                cmd(indicator)
                println("runAsyn end -> $title ${System.currentTimeMillis() - start}")
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    fun invoke(wait: Boolean = false, cmd: () -> Unit) {
        if (wait) ideaApp().invokeAndWait(cmd) else ideaApp().invokeLater(cmd)
    }

    fun cacheDir() = File(System.getProperty("user.home"), ".idea/xmodule").also { if (!it.exists()) it.mkdirs() }

    fun invokeWrite(cmd: () -> Unit) = invoke { ideaApp().invokeLaterOnWriteThread(cmd) }

    fun String.putSp(value: String?, project: Project? = null) {
        if (maps.put(real(this, project), value) != value) invokeWrite { PropertiesUtils.writeProperties(File(cacheDir(), "share.xml"), maps) }
    }

    fun String.getSp(default: String? = null, project: Project? = null) = if (maps.containsKey(real(this, project))) maps[real(this, project)] else default

    fun real(key: String, project: Project?): String = key + (project?.basePath?.hashCode()?.toString() ?: "")
    fun <T> Boolean?.getOrElse(get: T, e: T) = if (this == true) get else e

    @JvmStatic
    fun put(key: String, value: String?) = key.putSp(value, null)

    @JvmStatic
    fun get(key: String, default: String? = null) = key.getSp(default, null)

    fun openFile(project: Project, file: File) = FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(file, false)!!, true)

    fun log(msg: String?, event: Boolean = true, project: Project? = null) {
        if (msg.isNullOrBlank()) return
        if (event) {
            LOG_EVENT.createNotification("", "", msg, NotificationType.INFORMATION).notify(project)
        } else {
            LOG.debug(msg)
        }
    }

    fun copy(text: String) = Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)

    fun syncVcs(project: Project, syncVcs: Boolean) = invoke {
        val gits: List<ProjectModel> = XmlHelper.loadManifest(project.basePath)?.projects ?: emptyList()
        val codeDir = File(project.basePath, XmlHelper.loadConfig(project.basePath!!).codeRoot).canonicalPath
        val dirs = gits.mapNotNull { File(codeDir, it.path).takeIf { f -> f.exists() } }.plus(File(project.basePath, EnvKeys.BASIC))

        if (syncVcs) {
            //根据导入的CodeRoot目录,自动更改AS的版本管理
            val pVcs: ProjectLevelVcsManagerImpl = ProjectLevelVcsManagerImpl.getInstance(project) as ProjectLevelVcsManagerImpl
            pVcs.directoryMappings = dirs.filter { GitUtil.isGitRoot(it) }.map { VcsDirectoryMapping(it.absolutePath, "Git") }
            pVcs.notifyDirectoryMappingChanged()
        } else {
            /**
             * 所有代码的跟目录
             * 对比一下,当前导入的所有工程,是否都在version管理中,如果没有,提示用户进行管理
             */
            /**
             * 所有代码的跟目录
             * 对比一下,当前导入的所有工程,是否都在version管理中,如果没有,提示用户进行管理
             */
            val controlPaths = VcsRepositoryManager.getInstance(project).repositories.map { it.presentableUrl }
            val unHandle = dirs.filter { !controlPaths.contains(it.absolutePath) }
            if (unHandle.isNotEmpty())
                Messages.showMessageDialog("Those project had import but not in Version Control\n ${unHandle.joinToString { "\n" + it }} \n Please check Setting -> Version Control After Sync!!", "Miss Vcs Control", null)
        }
    }

}


