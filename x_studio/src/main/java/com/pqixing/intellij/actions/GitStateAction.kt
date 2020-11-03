package com.pqixing.intellij.actions

import com.intellij.dvcs.DvcsUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.AbstractVcsHelper
import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.adapter.JListSelectAdapter
import com.pqixing.intellij.adapter.JlistSelectListener
import com.pqixing.intellij.group.XGroup
import com.pqixing.intellij.ui.FileListDialog
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.GitHelper
import git4idea.GitVcs
import git4idea.repo.GitRepository
import java.awt.Dimension
import java.io.File
import javax.swing.JList

class GitStateAction : BaseGitAction, JlistSelectListener {
    //    val TIP = "A:All;  C:Conflict;  P:Push";
    var commitMsg = ""//提交的文本
    override fun update(e: AnActionEvent) {
        e?.presentation?.isEnabledAndVisible = XGroup.isBasic(e?.project)
    }
    override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {
        val info = items.last()
        val repo = getRepo(info.title) ?: return true
        val unMergeFiles = DvcsUtil.findVirtualFilesWithRefresh(GitHelper.getUnmergedFiles(repo))
        if (!unMergeFiles.isEmpty()) {
            val files = AbstractVcsHelper.getInstance(project).showMergeDialog(unMergeFiles, GitVcs.getInstance(project).mergeProvider)
            unMergeFiles.removeAll(files)//删除所有合并后的文件
        } else {//如果没有待合并的文件，打开文件列表，方便查看和修改
            val changeFiles: List<String>? = GitHelper.state(project, repo)
            if (changeFiles != null && changeFiles.isNotEmpty()) {
                var datas = mutableListOf<JListInfo>()
                var files = mutableListOf<File>()
                changeFiles.forEach {
                    val f = File(info.title, it.substring(3).trim())
                    files.add(f)
                    datas.add(JListInfo(f.name, it.substring(0, 2)))
                }
                ApplicationManager.getApplication().invokeLater {
                    val d = FileListDialog(project, datas, files) {
//                        GitHelper.
                        updateItemLog(info, "", "")
                    }
                    d.pack()
                    d.isVisible = true
                }
            }
        }
        //更新状态值
        updateItemLog(info, "", "")
        return true
    }

    constructor()
    constructor(repos: Map<String, GitRepository>) {
        allRepos.putAll(repos)
        createByMe = true
    }

    override fun checkUrls(urls: Map<String, String>): Boolean = true

    override fun initDialog(dialog: GitOperatorDialog) {
        dialog.setTargetBranch(null, false)
        dialog.jlTips.text = "Click item to resolve conflict or list files;"
        dialog.pOpertator.isVisible = false
        dialog.buttonOK.text = "Commit"
        dialog.buttonOK.isVisible = false
        dialog.buttonCancel.isVisible = false
        dialog.adapter.boxVisible = false
        dialog.adapter.selectListener = this
        dialog.jlBranch.text = dialog.jlBranch.text
        dialog.preferredSize = Dimension(600, 400)
    }

    override fun updateItemLog(info: JListInfo, operatorCmd: String, cacheLog: String?) {
        val repo = getRepo(info.title) ?: return
        val unMergeCount = GitHelper.getUnmergedFiles(repo).size
        val unClean = GitHelper.state(project, repo)?.size ?: 0
        if (unClean + unMergeCount == 0) {
            info.staue = 1
            info.log = "${repo.currentBranchName}:clear"
        } else {
            //如果有冲突，编辑黄色
            info.staue = if (unMergeCount > 0) 3 else 0
            info.log = "Conflict:$unMergeCount,Change:$unClean"
        }
    }

    override fun filterDatas(allDatas: MutableList<JListInfo>, operatorCmd: String): MutableList<JListInfo> {
        return super.filterDatas(allDatas, operatorCmd).apply { forEach { it.select = false } }
    }
    override fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> {
        val keys = allRepos.map { VfsUtil.urlToPath(it.key) }
        val afterUrls = if (keys.isEmpty()) urls else urls.filter { keys.contains(it.key) }
        return super.getAdapterList(afterUrls)
    }

    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean {
        dialog.isVisible = false
        val filter = allDatas.filter { it.staue == 3 }.map { it.title }
        if (filter.isNotEmpty()) {
            Messages.showMessageDialog("There are some conflict un resolve; \n${filter.joinToString { it + "\n" }}", "Conflict", null);
            dialog.isVisible = false
            return false
        }
        commitMsg = Messages.showInputDialog(project, "Input msg to commit", "Commit", null)
                ?: "Commit By Ide"
        if (commitMsg.trim().isEmpty()) commitMsg = "Commit By Ide";
        dialog.isVisible = true
        return true
    }

    override fun onOkData(allDatas: MutableList<JListInfo>): List<JListInfo> = allDatas

    override fun onOtherOk(cmd: String, dialog: GitOperatorDialog, targetBranch: String, r: JListInfo, project: Project) {
        val repo = getRepo(r.title) ?: return;
        r.log = GitHelper.addAndCommit(project, repo, commitMsg, dialog.gitListener)
        r.staue = if (r.log == "Success") 1 else 3
    }
}