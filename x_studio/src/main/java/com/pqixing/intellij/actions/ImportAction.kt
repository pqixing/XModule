package com.pqixing.intellij.actions

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.actionSystem.ActionManager
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
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.Config
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.group.XModuleGroup
import com.pqixing.intellij.ui.NewImportDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.intellij.utils.UiUtils.realName
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import java.io.File


class ImportAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = XModuleGroup.hasBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXml = XmlHelper.loadManifest(basePath)
        if (projectXml == null) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val config = XmlHelper.loadConfig(basePath)
        val includes = config.include
        val codeRoot = config.codeRoot
        val dependentModel = config.dependentModel

        val imports = includes.replace("+", ",").split(",").mapNotNull { if (it.trim().isEmpty()) null else it.trim() }.toList()
        val infos = projectXml.allModules().filter { it.attach == null }.map { JListInfo(it.path.substringBeforeLast("/") + "/" + it.name, "${it.desc} - ${it.type.substring(0, 3)} ") }.toMutableList()

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
                saveConfig(config, dialog)
                val allIncludes = XmlHelper.parseInclude(projectXml, dialog.imports.toSet())

                val codePath = File(basePath, dialog.codeRootStr).canonicalPath
                //下载代码
                val gitPaths = projectXml.allModules().filter { allIncludes.contains(it.name) }
                        .map { it.project }.toSet().map { File(codePath, it.path) to it.url }.toMap().toMutableMap()

                gitPaths.filter { !GitUtil.isGitRoot(it.key) }.forEach {
                    indicator.text = "Start Clone... ${it.value} "

                    FileUtils.delete(it.key)
                    //下载master分支
                    GitHelper.clone(project, it.key, it.value, dialog.selectBranch)
                }
//                    如果快速导入不成功,则,同步一次
                ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)

                disposeModule(project, allIncludes, dialog.codeRootStr == codeRoot)
                //添加basic的地址
                Thread.sleep(2000)//先睡眠2秒,然后检查git管理是否有缺少
                ApplicationManager.getApplication().invokeLater {
                    syncVcs(gitPaths.keys.toMutableSet().also { it.add(File(basePath, EnvKeys.BASIC)) }, dialog.syncVcs(), project)
                }
            }
        }
        dialog.btnConfig.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(XmlHelper.fileConfig(basePath), false)!!, true)
        }
        dialog.btnProjectXml.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(XmlHelper.fileManifest(basePath), false)!!, true)
        }
        dialog.setOnOk {
            //切换根目录的分支
            if (localBranch == dialog.selectBranch) ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            else GitHelper.checkout(project, dialog.selectBranch, mutableListOf(repo)) {
                ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            }
        }
    }

    private fun syncVcs(dirs: MutableSet<File>, syncVcs: Boolean, project: Project) {
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


    /**
     * 直接通过ide进行导入
     */
    private fun disposeModule(project: Project, allIncludes: MutableSet<String>, codeRootChange: Boolean) = ApplicationManager.getApplication().invokeLater {

        val projectName = project.name.trim().replace(" ", "")
        val manager = ModuleManager.getInstance(project)

        manager.modules.forEach { m ->
            if (projectName == m.name) return@forEach
            if (codeRootChange || !allIncludes.contains(m.realName())) kotlin.runCatching { manager.disposeModule(m) }
        }

    }

    private fun saveConfig(config: Config, dialog: NewImportDialog) = ApplicationManager.getApplication().invokeLater {
        ApplicationManager.getApplication().runWriteAction {
            config.dependentModel = dialog.dpModel?.trim() ?: ""
            config.codeRoot = dialog.codeRootStr.trim()
            config.include = dialog.imports.filter { it.isNotEmpty() }.joinToString(",")
            XmlHelper.saveConfig(basePath, config)
        }
    }
}
