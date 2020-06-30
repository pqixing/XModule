package com.pqixing.intellij.actions

import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.android.tools.idea.gradle.project.sync.GradleSyncListener
import com.android.tools.idea.util.toIoFile
import com.google.wireless.android.sdk.stats.GradleSyncStats
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.group.QToolGroup
import com.pqixing.intellij.ui.NewImportDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.intellij.utils.UiUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import git4idea.GitUtil
import groovy.lang.GroovyClassLoader
import java.io.File
import java.util.*


class ImportAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = QToolGroup.hasBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val xmlFIle = File(basePath, EnvKeys.XML_PROJECT)
        val configFile = File(basePath, "Config.java")
        if (!xmlFIle.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val projectXml = XmlHelper.parseManifest(xmlFIle)
        val clazz = GroovyClassLoader().parseClass(configFile)
        val newInstance = clazz.newInstance()
        val includes = clazz.getField("include").get(newInstance).toString()
        var codeRoot = clazz.getField("codeRoot").get(newInstance).toString()
        val dependentModel = clazz.getField("dependentModel").get(newInstance).toString()

        val imports = includes.replace("+", ",").split(",").mapNotNull { if (it.trim().isEmpty()) null else it.trim() }.toList()
        val infos = projectXml.allModules().filter { it.attach == null }.map { JListInfo(it.path.substringBeforeLast("/")+"/"+it.name, "${it.desc} - ${it.type.substring(0,3)} ") }.toMutableList()

        val repo = GitHelper.getRepo(File(basePath, EnvKeys.BASIC), project)
        val branchs = repo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }.toMutableList()
        val localBranch = repo.currentBranchName ?: "master"
        branchs.remove(localBranch)
        branchs.add(0, localBranch)

        val dialog = NewImportDialog(project, imports.toMutableList(), infos, branchs, dependentModel, codeRoot)
        dialog.pack()
        dialog.isVisible = true
        val importTask = object : Task.Backgroundable(project, "Start Import") {
            override fun run(indicator: ProgressIndicator) {
                saveConfig(configFile, dialog)
                val allIncludes = XmlHelper.parseInclude(projectXml,dialog.imports.toSet())

                val codePath = File(basePath, dialog.codeRootStr).canonicalPath
                //下载代码
                val gitPaths = projectXml.allModules().filter { allIncludes.contains(it.name) }
                        .map { it.project }.toSet().map { File(codePath, it.path) to it.url }.toMap().toMutableMap()
                gitPaths[File(basePath, EnvKeys.BASIC)] = projectXml.basicUrl

                gitPaths.filter { !GitUtil.isGitRoot(it.key) }.forEach {
                    indicator.text = "Start Clone... ${it.value} "

                    FileUtils.delete(it.key)
                    //下载master分支
                    GitHelper.clone(project, it.key, it.value, dialog.selectBranch)
                }

                val pimls = allIml()
                val importImls = projectXml.allModules().filter { allIncludes.contains(it.name) }
                        .mapNotNull { pimls[File(codePath, it.path).canonicalPath]?.toString() }

                ApplicationManager.getApplication().invokeLater { importByIde(project, importImls.toMutableList()) }
                //如果快速导入不成功,则,同步一次
//                /*if (!import)*/ ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)
                GradleSyncInvoker.getInstance().requestProjectSync(project, GradleSyncStats.Trigger.TRIGGER_USER_SYNC_ACTION, object : GradleSyncListener {
                    override fun syncSucceeded(project: Project) {
                        syncVcs(gitPaths, dialog.syncVcs(), project)

                        val manager = ModuleManager.getInstance(project)
                        manager.modules.forEach {
                            //模块的代码目录
                            pimls[ModuleRootManager.getInstance(it).contentRoots[0].toIoFile().canonicalPath] = it.moduleFilePath
                        }
                        saveIml(pimls)
                    }
                })
            }
        }
        dialog.btnConfig.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(configFile, false)!!, true)
        }
        dialog.btnProjectXml.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(xmlFIle, false)!!, true)
        }
        dialog.btnDepend.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(File(xmlFIle.parentFile,"depend.gradle"), false)!!, true)
        }
        dialog.setOnOk {
            //切换根目录的分支
            if (localBranch == dialog.selectBranch) ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            else GitHelper.checkout(project, dialog.selectBranch, mutableListOf(repo)) {
                ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            }
        }
    }

    private fun syncVcs(gitPaths: MutableMap<File, String>, syncVcs: Boolean, project: Project) {
        val dirs = gitPaths.keys
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
            val controlPaths = VcsRepositoryManager.getInstance(project).repositories.map { it.presentableUrl }
            dirs.removeIf { controlPaths.contains(it.absolutePath) }
            if (dirs.isNotEmpty())
                Messages.showMessageDialog("Those project had import but not in Version Control\n ${dirs.joinToString { "\n" + it }} \n Please check Setting -> Version Control After Sync!!", "Miss Vcs Control", null)
        }
    }
    fun allIml() = PropertiesUtils.readProperties(File(project.basePath, UiUtils.IML_PROPERTIES))
    fun saveIml(pros: Properties) = UiUtils.invokeLaterOnWriteThread(Runnable { PropertiesUtils.writeProperties(File(project.basePath, UiUtils.IML_PROPERTIES), pros) })

    private fun saveConfig(configgFile: File, dialog: NewImportDialog) = ApplicationManager.getApplication().invokeLater {
        ApplicationManager.getApplication().runWriteAction {
            val dpModel = dialog.dpModel?.trim() ?: ""
            val codeRoot = dialog.codeRootStr.trim()
            val includes = dialog.imports.filter { it.isNotEmpty() }

            var result = configgFile.readText()
            result = result.replace(Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
            result = result.replace(Regex("String *codeRoot *=.*;"), "String codeRoot = \"$codeRoot\";")
            result = result.replace(Regex("String *include *=.*;"), "String include = \"${includes.joinToString(",")}\";")
            FileUtils.writeText(configgFile, result, true)
        }
    }

    /**
     * 直接通过ide进行导入
     */
    private fun importByIde(project: Project, importImls: MutableList<String>) = ApplicationManager.getApplication().invokeLater {

        val projectName = project.name.trim().replace(" ", "")
        val manager = ModuleManager.getInstance(project)


        manager.modules.forEach { m ->
            if (importImls.remove(m.moduleFilePath) || projectName == m.name) return@forEach
            manager.disposeModule(m)
        }
        ApplicationManager.getApplication().runWriteAction {
            importImls.forEach { i -> loadModule(manager, i) }
        }
    }

    /**
     * 加载模块
     */
    fun loadModule(manager: ModuleManager, filePath: String) {
        if (File(filePath).exists()) try {
            manager.loadModule(filePath)
        } finally {
        }
    }
}
