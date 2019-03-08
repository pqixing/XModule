package com.pqixing.intellij.actions

import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.Git4IdeHelper
import git4idea.GitUtil
import git4idea.repo.GitRepository
import java.io.File

class GitCheckoutAction : BaseGitAction() {
    override fun checkUrls(urls: Map<String, String>): Boolean = true

    override fun initDialog(dialog: GitOperatorDialog) {
        dialog.setTargetBranch(rootRepo.branches.remoteBranches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }, true)
        dialog.pOpertator.isVisible = false
    }

    override fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> {
        val allDatas = urls.filter { GitUtil.isGitRoot(it.key) }.map {
            JListInfo(it.key, select = true)
        }.toMutableList()
        allDatas.add(0, JListInfo("$basePath/templet", select = true))
        allRepos.putAll(allDatas.filter { it.select }.map {
            val repo = Git4IdeHelper.getRepo(File(it.title), project)
            it.log = repo.currentBranchName ?: ""
            Pair(it.title, repo)
        })
        return allDatas
    }

    override fun doOk(dialog: GitOperatorDialog, allDatas: MutableList<JListInfo>, urls: Map<String, String>, rootBranch: String?) {
//        if (rootBranch == dialog.targetBranch) {
//            dialog.dispose()
//            Messages.showMessageDialog(project, "Target branch can not equals local branch!!", dialog.targetBranch, null)
//            return
//        }
        dialog.dispose()
        val selectItem = allDatas.filter { it.select }.map { it.title }
        val selectRepo = allRepos.filter { selectItem.contains(it.key) }
        //切换完成,更新状态
        Git4IdeHelper.checkout(project, dialog.targetBranch, selectRepo.values.toList()) {
            GitStateAction(selectRepo).actionPerformed(e)
        }
    }
    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean = true
}