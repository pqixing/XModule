package com.pqixing.intellij.actions

import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.Git4IdeHelper
import git4idea.GitUtil
import git4idea.repo.GitRepository
import java.io.File

class GitBranchAction : BaseGitAction() {
    override fun beforeActionRun() = key == Messages.showInputDialog("Please input key!!", "Password", null)
    override fun checkUrls(urls: Map<String, String>): Boolean {
        val filter = urls.keys.filter { !GitUtil.isGitRoot(it) }
        if (filter.isNotEmpty()) {
            Messages.showMessageDialog(project, "Those project are not clone!!\n${filter.joinToString { it + "\n" }}", "Miss Project", null)
            return false
        }
        return true
    }

    override fun initDialog(dialog: GitOperatorDialog) {
        dialog.adapter.boxVisible = false
        dialog.setOperator("create", "merge", "delete")
        dialog.setTargetBranch(rootRepo.branches.remoteBranches.map { it.name }, true)
        dialog.allButton.isVisible = false//不允许反选
        dialog.setOnOperatorChange { }//重新设置，不需要更新状态
    }

    override fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> {
        val allDatas = urls.map {
            JListInfo(it.key, select = true)
        }.toMutableList()
        allDatas.add(0, JListInfo("$basePath/templet", select = true))
        return allDatas
    }

    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean {
        val localBranch = rootRepo.currentBranchName
        if (localBranch == null) {
            Messages.showMessageDialog(project, "Can not find local branch for root project!!", "Miss Branch", null)
            return false
        }
        val targetBranch = dialog.targetBranch.substring(Math.max(0, dialog.targetBranch.lastIndexOf("/")))
        if ("origin/$localBranch" == targetBranch) {
            Messages.showMessageDialog(project, "Target branch are not equals local branch!!", localBranch, null)
            return false
        }
        val branchs = allDatas.mapNotNull { if (localBranch == it.log) null else "${it.title}  ->  ${it.log}" }
        if (branchs.isNotEmpty()) {
            Messages.showMessageDialog(project, "Those project branch are not equals local branch!! \n ${branchs.joinToString { it + "\n" }}", localBranch, null)
            return false
        }
        return true
    }
}