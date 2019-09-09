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
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.NewImportDialog
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.SubModuleType
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import groovy.lang.GroovyClassLoader
import java.io.File


class ImportAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String

    override fun update(e: AnActionEvent?) {
        e?.presentation?.isEnabledAndVisible = QToolGroup.isModulariztionProject(e?.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")
        if (!projectXmlFile.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(configFile)
        val newInstance = clazz.newInstance()
        val includes = clazz.getField("include").get(newInstance).toString()
        var codeRoot = clazz.getField("codeRoot").get(newInstance).toString()
        val dependentModel = clazz.getField("dependentModel").get(newInstance).toString()

        val imports = includes.replace("+", ",").split(",").mapNotNull { if (it.trim().isEmpty()) null else it.trim() }.toList()
        val infoMaps = projectXml.allSubModules().filter { it.type != SubModuleType.TYPE_LIBRARY_API }.map { Pair(it.name, JListInfo(it.name, it.introduce.trim())) }.toMap(mutableMapOf())
        val repo = GitHelper.getRepo(File(basePath, "templet"), project)
        val branchs = repo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }.toMutableList()
        val localBranch = repo.currentBranchName ?: "master"
        branchs.remove(localBranch)
        branchs.add(0, localBranch)

        val dialog = NewImportDialog(project, imports.toMutableList(), infoMaps.values.toMutableList(), branchs, dependentModel, codeRoot)
        dialog.pack()
        dialog.isVisible = true
        val importTask = object : Task.Backgroundable(project, "Start Import") {
            override fun run(indicator: ProgressIndicator) {
                val codePath = File(basePath, dialog.codeRootStr).canonicalPath
                saveConfig(configFile, dialog)
                val includeMaps = getImport(projectXml, dialog.imports.filter { !it.contains("#") }.toMutableSet(), codePath, dialog.imports.filter { it.contains("#") })
                val import = dialog.importModel == "Import" && importByIde(includeMaps)

                val maps = projectXml.allSubModules()
                        .filter { includeMaps.containsKey(it.name) }
                        .map { Pair(codePath + "/" + it.project.name, it.project.url) }
                        .toMap(mutableMapOf())
                maps.forEach { map ->
                    File(map.key).apply {
                        if (GitUtil.isGitRoot(this)) return@apply
                        if (exists()) FileUtils.delete(this)
                        indicator.text = "Clone... ${map.value} "
                        //下载master分支
                        GitHelper.clone(project, this, map.value, dialog.selectBranch)
                    }
                }

                //如果快速导入不成功,则,同步一次
                if (!import) ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)
                Thread.sleep(2000)//先睡眠2秒,然后检查git管理是否有缺少
                ApplicationManager.getApplication().invokeLater {
                    if (dialog.syncVcs()) {
                        //根据导入的CodeRoot目录,自动更改AS的版本管理
                        val pVcs: ProjectLevelVcsManagerImpl = ProjectLevelVcsManagerImpl.getInstance(project) as ProjectLevelVcsManagerImpl
                        pVcs.directoryMappings = projectXml.projects
                                .map { codePath + "/" + it.name }
                                .filter { GitUtil.isGitRoot(File(it)) }
                                .map { VcsDirectoryMapping(it, "Git") }
                                .toMutableList().apply {
                                    add(0, VcsDirectoryMapping(File(basePath, "templet").canonicalPath, "Git"))
                                }
                        pVcs.notifyDirectoryMappingChanged()
                    } else {
                        /**
                         * 所有代码的跟目录
                         * 对比一下,当前导入的所有工程,是否都在version管理中,如果没有,提示用户进行管理
                         */
                        val controlPaths = VcsRepositoryManager.getInstance(project).repositories.filter { it.presentableUrl.startsWith(codePath) }.map { it.presentableUrl }
                        val gitPaths = maps.keys
                        gitPaths.removeAll(controlPaths)
                        if (gitPaths.isNotEmpty())
                            Messages.showMessageDialog("Those project had import but not in Version Control\n ${gitPaths.joinToString { "\n" + it }} \n Please check Setting -> Version Control After Sync!!", "Miss Vcs Control", null)
                    }
                }

            }
        }
        dialog.btnConfig.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(configFile, false)!!, true)
        }
        dialog.btnProjectXml.addActionListener {
            dialog.dispose()
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(projectXmlFile, false)!!, true)
        }
        dialog.setOnOk {
            //切换根目录的分支
            if (localBranch == dialog.selectBranch) ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            else GitHelper.checkout(project, dialog.selectBranch, mutableListOf(repo)) {
                ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
            }
        }
    }

    /**
     * 解析需要导入的工程
     */
    private fun getImport(projectXml: ProjectXmlModel, includes: MutableSet<String>, codePath: String, moreInclude: List<String>): Map<String, String> {
        if (moreInclude.isNotEmpty()) moreInclude.sortedWith(Comparator { t, t1 ->
            (if (t.contains("#")) t.substring(0, 1) else "A").compareTo(if (t1.contains("#")) t1.substring(0, 1) else "A")
        }).forEach { v ->
            val l = v.trim().replace(Regex(".*#"), "")
            if (!v.contains("#")) includes.add(l)
            else if (v.startsWith("E#")) includes.remove(l)
            else if (v.startsWith("D#")) includes.addAll(handleDps(File(basePath), l))
            else if (v.startsWith("ED#")) includes.removeAll(handleDps(File(basePath), l))
        }

        return includes.map { Pair(it, getImlPath(codePath, projectXml, it)) }.toMap(mutableMapOf())
    }

    private fun handleDps(rootDir: File, module: String) = FileUtils.readText(File(rootDir, "build/dps/$module.dp"))?.split(",")?.map { it.trim() }?.toSet()
            ?: emptySet()

    private fun getImlPath(codePath: String, projectXml: ProjectXmlModel, title: String) = "$codePath/${projectXml.findSubModuleByName(title)?.path}/$title.iml"

    private fun saveConfig(configgFile: File, dialog: NewImportDialog) = ApplicationManager.getApplication().invokeLater {
        ApplicationManager.getApplication().runWriteAction {
            val dpModel = dialog.dpModel?.trim() ?: ""
            val codeRoot = dialog.codeRootStr.trim()
            val includes = dialog.imports

            var result = configgFile.readText()
            result = result.replace(Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
            result = result.replace(Regex("String *codeRoot *=.*;"), "String codeRoot = \"$codeRoot\";")
            result = result.replace(Regex("String *include *=.*;"), "String include = \"${includes.joinToString { "$it," }}\";")
            FileUtils.writeText(configgFile, result, true)
        }
    }

    /**
     * 直接通过ide进行导入
     */
    private fun importByIde(includes: Map<String, String>): Boolean {
        val imls = includes.map { it.value }.filter { File(it).exists() }.toMutableSet()
        val projectName = project.name.trim().replace(" ", "")
        ApplicationManager.getApplication().invokeLater {
            val manager = ModuleManager.getInstance(project)
            manager.modules.forEach { m ->
                if (imls.remove(m.moduleFilePath) || projectName == m.name) return@forEach
                manager.disposeModule(m)
            }
            imls.forEach { i -> loadModule(manager, i) }
        }
        return imls.size == includes.size
    }

    /**
     * 加载模块
     */
    private fun loadModule(manager: ModuleManager, filePath: String) = ApplicationManager.getApplication().runWriteAction {
        if (File(filePath).exists()) try {
            manager.loadModule(filePath)
        } finally {
        }
    }
}
