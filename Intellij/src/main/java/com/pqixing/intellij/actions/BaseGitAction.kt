package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.Git4IdeHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import git4idea.history.GitLogUtil
import git4idea.repo.GitRepository
import groovy.lang.GroovyClassLoader
import java.io.File

abstract class BaseGitAction : AnAction() {
    var allRepos = mutableMapOf<String, GitRepository>()
    val key = "k1234"
    lateinit var project: Project
    lateinit var projectXml: ProjectXmlModel
    lateinit var codeRootDir: String
    lateinit var basePath: String
    lateinit var rootRepo: GitRepository
    lateinit var e: AnActionEvent
    var cacheLog = mutableMapOf<String, String>()
    override fun actionPerformed(e: AnActionEvent) {
        this.e = e
        project = e.project ?: return
        basePath = project.basePath ?: return
        if (!beforeActionRun()) return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")
        if (!projectXmlFile.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(configFile)
        var codeRoot = clazz.getField("codeRoot").get(clazz.newInstance()).toString()
        codeRootDir = File(basePath, codeRoot).canonicalPath
        val urls = projectXml.projects.map { Pair("$codeRootDir/${it.name}", it.url) }.toMap()
        if (!checkUrls(urls)) return
        val allDatas = getAdapterList(urls)

        rootRepo = getRepo("$basePath/templet")!!
        val branches = Git4IdeHelper.getRepo(File("$basePath/templet"), project).branches

        val branchNames = mutableListOf<String>()
        branchNames += branches.localBranches.map { it.name }
        branchNames += branches.remoteBranches.map { it.name }
        val rootBranch = rootRepo.currentBranchName;
        val dialog = GitOperatorDialog(this.javaClass.simpleName.replace("Action", ""), rootBranch, allDatas)
        dialog.setOnOperatorChange { updateLog(dialog) }
        initDialog(dialog)
        dialog.pack()
        updateLog(dialog)
        dialog.setOnOk {
            if (checkOnOk(allDatas, dialog)) doOk(dialog, allDatas, urls, rootBranch) else dialog.dispose()
        }
        dialog.isVisible = true
    }

    private fun updateLog(dialog: GitOperatorDialog) {
        ProgressManager.getInstance().runProcess({
            val cmd = dialog.operatorCmd
            dialog.adapter.datas.forEach {
                updateItemLog(it, cmd, cacheLog[cmd + it.title])
                cacheLog[cmd + it.title] = it.log
            }
            dialog.updateUI()
        }, null)
    }

    /**
     * 更新状态
     */
    protected open fun updateItemLog(info: JListInfo, operatorCmd: String, cacheLog: String?) {
        val repo = getRepo(info.title)
        when (operatorCmd) {
            "merge", "delete", "create" -> {
                info.log = cacheLog ?: repo?.currentBranchName ?: "No Branch"
                info.staue = if (rootRepo.currentBranchName == info.log) 0 else 3
            }
            "clone" -> {
                info.log = cacheLog ?: repo?.currentBranchName ?: "Project No Exists"
                info.staue = if (repo == null) 3 else 0
            }
            "update" -> {
                info.log = cacheLog ?: repo?.let { it.currentBranchName + " : " + it.state.toString().toLowerCase() }
                        ?: "Project No Exists"
                info.staue = if (repo == null) 3 else 0
            }
            "push" -> {
                info.log = cacheLog ?: repo?.let {
                    it.currentBranchName + " : " + GitLogUtil.collectFullDetails(project, repo.root, "origin/${it.currentBranchName}..${it.currentBranchName}").size
                } ?: "Project No Exists"
                info.staue = if (repo == null) 3 else 0
            }
        }
    }

    protected open fun doOk(dialog: GitOperatorDialog, allDatas: MutableList<JListInfo>, urls: Map<String, String>, rootBranch: String?) {
        val importTask = object : Task.Backgroundable(project, "Start Import") {
            override fun run(indicator: ProgressIndicator) {
                dialog.gitListener.setIndicator(indicator)
                val operatorCmd = dialog.operatorCmd
                val repos = allDatas.filter { it.select }
                indicator.text = "start $operatorCmd"
                when (operatorCmd) {
                    "clone" -> for (r in repos) clone(r, project, urls, rootBranch, dialog)
                    "update" -> for (r in repos) update(r, dialog)
                    "push" -> for (r in repos) push(r, dialog)
                    "merge" -> if ("origin/$rootBranch" != dialog.targetBranch) for (r in repos) merge(dialog, dialog.targetBranch, r, project)
                    "delete" -> {
                    }
                    "create" -> {
                    }
                }
                indicator.text = "end $operatorCmd"
                dialog.updateUI()
                afterDoOk(dialog)
            }


        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    protected open fun afterDoOk(dialog: GitOperatorDialog) {

    }

    protected open fun beforeActionRun(): Boolean = true


    abstract fun checkUrls(urls: Map<String, String>): Boolean

    abstract fun initDialog(dialog: GitOperatorDialog)

    abstract fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo>

    abstract fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean

    protected fun merge(dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project) {
        if (!GitUtil.isGitRoot(r.title)) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val merge = Git4IdeHelper.merge(project, targetBranch, getRepo(r.title), dialog.gitListener)
        r.log = merge
        r.staue = if ("Up-To-Date" == merge || "Merge Success" == merge) 1 else 3
        dialog.updateUI()
    }

    protected fun update(r: JListInfo, dialog: GitOperatorDialog) {
        if (!GitUtil.isGitRoot(r.title)) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val update = Git4IdeHelper.update(project, getRepo(r.title), dialog.gitListener)
        r.log = update
        r.staue = if ("Up-To-Date" == update || "Merge Success" == update) 1 else 3
        dialog.updateUI()
    }

    protected fun push(r: JListInfo, dialog: GitOperatorDialog) {
        if (!GitUtil.isGitRoot(r.title)) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val update = Git4IdeHelper.push(project, getRepo(r.title), dialog.gitListener)
        r.log = update
        r.staue = if ("Success" == update) 1 else 3
        dialog.updateUI()
    }

    protected fun clone(r: JListInfo, project: Project, urls: Map<String, String>, rootBranch: String?, dialog: GitOperatorDialog) {
        if (GitUtil.isGitRoot(r.title)) {
            r.log = "Project Exists"
            r.staue = 1
            return
        }
        val clone = Git4IdeHelper.clone(project, File(r.title).apply { FileUtils.delete(this) }, urls.getValue(r.title), rootBranch, dialog.gitListener)
        r.log = if (clone == null) "Clone Fail" else "Clone Success"
        r.staue = if (clone == null) 3 else 1
        dialog.updateUI()
    }

    /**
     * 如果找不到
     */
    protected fun getRepo(path: String): GitRepository? {
        var repository = allRepos[path]
        if (!GitUtil.isGitRoot(path)) return null;
        if (repository == null) {
            repository = Git4IdeHelper.getRepo(File(path), project)
            allRepos[path] = repository
        }
        return repository
    }
}