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
import com.pqixing.intellij.utils.GitHelper
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import git4idea.repo.GitRepository
import groovy.lang.GroovyClassLoader
import java.io.File

abstract class BaseGitAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    lateinit var rootRepoPath: String
    var createByMe = false;
    var allRepos = mutableMapOf<String, GitRepository>()
    val key = "k1234"
    lateinit var e: AnActionEvent
    var cacheLog = mutableMapOf<String, String>()
    override fun actionPerformed(e: AnActionEvent) {
        this.e = e
        this.project = e.project ?: return
        this.basePath = project.basePath ?: return
        rootRepoPath = "$basePath/templet";
        if (!beforeActionRun()) return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")
        if (!projectXmlFile.exists() || !configFile.exists()) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        if (!createByMe) resetCache()
        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(configFile)
        var codeRoot = clazz.getField("codeRoot").get(clazz.newInstance()).toString()
        val codeRootDir = File(basePath, codeRoot).canonicalPath
        val urls = projectXml.projects.map { Pair("$codeRootDir/${it.name}", it.url) }.toMap()
        if (!checkUrls(urls)) return
        val allDatas = getAdapterList(urls).apply { sortBy { it.title } }//按照标题排序

        val rootRepo = getRepo(rootRepoPath)!!
        val branches = GitHelper.getRepo(File("$basePath/templet"), project).branches

        val branchNames = mutableListOf<String>()
        branchNames += branches.localBranches.map { it.name }
        branchNames += branches.remoteBranches.map { it.name }
        val rootBranch = rootRepo.currentBranchName;
        val dialog = GitOperatorDialog(project, this.javaClass.simpleName.replace("Action", ""), rootBranch, allDatas)
        dialog.setOnOperatorChange {
            dialog.adapter.setDatas(filterDatas(allDatas, dialog.operatorCmd))
            updateLog(dialog)
        }
        initDialog(dialog)
        dialog.pack()
        dialog.adapter.setDatas(filterDatas(allDatas, dialog.operatorCmd))
        updateLog(dialog)
        dialog.setOnOk {
            if (checkOnOk(allDatas, dialog)) doOk(dialog, dialog.adapter.datas, urls, rootBranch)
        }
        dialog.isVisible = true
    }

    protected open fun filterDatas(allDatas: MutableList<JListInfo>, operatorCmd: String): MutableList<JListInfo> = when (operatorCmd) {
        "clone" -> allDatas.sortedBy { GitUtil.isGitRoot(File(it.title)) }.toMutableList()
        else -> allDatas.filter { GitUtil.isGitRoot(File(it.title)) }.toMutableList()
    }

    protected open fun resetCache() {
        allRepos.clear()
        cacheLog.clear()

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
                info.staue = if (getRepo(rootRepoPath)?.currentBranchName == info.log) 0 else 3
            }
            "clone" -> {
                info.log = cacheLog ?: repo?.currentBranchName ?: "No Clone"
                info.staue = if (repo == null) 3 else 0
            }
            "update" -> {
                info.log = cacheLog
                        ?: repo?.currentBranch?.let { GitHelper.state(project, repo)?.size?.toString() + " -> " + it.findTrackedBranch(repo)?.name }
                                ?: "Project No Exists"
                info.staue = if (repo == null) 3 else 0
            }
            "push" -> {
                info.log = cacheLog
                        ?: repo?.currentBranch?.let { GitHelper.state(project, repo)?.size?.toString() + " -> " + it.findTrackedBranch(repo)?.name }
                                ?: "Project No Exists"
                info.staue = if (repo == null) 3 else 0
            }
        }
    }

    protected open fun doOk(dialog: GitOperatorDialog, allDatas: MutableList<JListInfo>, urls: Map<String, String>, rootBranch: String?) {
        val importTask = object : Task.Backgroundable(project, "Start Import") {
            override fun run(indicator: ProgressIndicator) {
                dialog.gitListener.setIndicator(indicator)
                val operatorCmd = dialog.operatorCmd
                val repos = onOkData(allDatas)
                indicator.text = "start $operatorCmd"
                when (operatorCmd) {
                    "clone" -> for (r in repos) clone(r, project, urls, rootBranch, dialog)
                    "update" -> for (r in repos) update(r, dialog)
                    "push" -> for (r in repos) push(r, dialog)
                    "merge" -> for (r in repos) merge(dialog, dialog.targetBranch, r, project)
                    "delete" -> for (r in repos) delete(dialog, dialog.targetBranch, r, project)
                    "create" -> for (r in repos) create(dialog, dialog.targetBranch, r, project)
                    else -> for (r in repos) onOtherOk(operatorCmd, dialog, dialog.targetBranch, r, project)
                }
                indicator.text = "end $operatorCmd"
                dialog.updateUI()
                afterDoOk(dialog)
            }


        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }

    protected open fun onOkData(allDatas: MutableList<JListInfo>) = allDatas.filter { it.select }

    protected open fun onOtherOk(cmd: String, dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project) {}

    private fun create(dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project) {
        if (!GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val create = GitHelper.create(project, targetBranch, getRepo(r.title), dialog.gitListener)
        r.log = create
        r.staue = if (create == "Success") 1 else 3
        dialog.updateUI()
    }

    private fun delete(dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project?) {
        if (!GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val delete = GitHelper.delete(project, targetBranch, getRepo(r.title), dialog.gitListener)
        r.log = delete
        r.staue = if (delete == "Success") 1 else 3
        dialog.updateUI()

    }

    protected open fun afterDoOk(dialog: GitOperatorDialog) {
        dialog.btnRevert.isVisible = true
        cacheLog.clear()//清楚缓存记录
        allRepos.clear()//清楚git，防止内存持续占用
//        allRepos.forEach { it.value.update() }//更新git操作
//        updateLog(dialog)//重新更新数据
    }

    protected open fun beforeActionRun(): Boolean = true


    abstract fun checkUrls(urls: Map<String, String>): Boolean

    abstract fun initDialog(dialog: GitOperatorDialog)

    protected open fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> {
        val allDatas = urls.map {
            JListInfo(it.key, select = true)
        }.toMutableList()
        allDatas.add(0, JListInfo(rootRepoPath, select = true))
        return allDatas
    }

    abstract fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean

    protected fun merge(dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project) {
        if (!GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val repo = getRepo(r.title)
        r.log = if (!GitHelper.checkBranchExists(repo, targetBranch)) "Branch Not Exists" else GitHelper.merge(project, targetBranch, repo, dialog.gitListener)
        r.staue = if ("Already up to date" == r.log || "Merge Success" == r.log) 1 else 3
        dialog.updateUI()
    }

    protected fun update(r: JListInfo, dialog: GitOperatorDialog) {
        if (!GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val update = GitHelper.update(project, getRepo(r.title), dialog.gitListener)
        r.log = update
        r.staue = if ("Already up to date" == update || "Merge Success" == update) 1 else 3
        dialog.updateUI()
    }

    protected fun push(r: JListInfo, dialog: GitOperatorDialog) {
        if (!GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Not Exists"
            r.staue = 3
            return
        }
        val update = GitHelper.push(project, getRepo(r.title), dialog.gitListener)
        r.log = update
        r.staue = if ("Success" == update) 1 else 3
        dialog.updateUI()
    }

    protected fun clone(r: JListInfo, project: Project, urls: Map<String, String>, rootBranch: String?, dialog: GitOperatorDialog) {
        if (GitUtil.isGitRoot(File(r.title))) {
            r.log = "Project Exists"
            r.staue = 1
            return
        }
        val clone = GitHelper.clone(project, File(r.title).apply { FileUtils.delete(this) }, urls.getValue(r.title), rootBranch, dialog.gitListener)
        r.log = if (clone == null) "Clone Fail" else "Clone Success"
        r.staue = if (clone == null) 3 else 1
        dialog.updateUI()
    }

    /**
     * 如果找不到
     */
    protected fun getRepo(path: String): GitRepository? {
        var repository = allRepos[path]
        if (!GitUtil.isGitRoot(File(path))) return null;
        if (repository == null) {
            repository = GitHelper.getRepo(File(path), project)
            allRepos[path] = repository
        }
        return repository
    }
}