package com.pqixing.intellij.actions

import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.GitHelper
import git4idea.GitUtil

class GitBranchAction : BaseGitAction() {
    override fun beforeActionRun() = key == Messages.showPasswordDialog("Please input key!!", "Password")
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
        dialog.setTargetBranch(getRepo(rootRepoPath)?.branches?.remoteBranches?.map { it.name }, true)
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
        val localBranch = getRepo(rootRepoPath)?.currentBranchName
        if (localBranch == null) {
            Messages.showMessageDialog(project, "Can not find local branch for root project!!", "Miss Branch", null)
            return false
        }
        val operatorCmd = dialog.operatorCmd
        //不允许操作master
        if ("delete" == operatorCmd && "master" == GitHelper.findLocalBranchName(dialog.targetBranch)) {
            Messages.showErrorDialog(project, "Can not delete branch : master", "Error")
            return false
        }
        if (localBranch == GitHelper.findLocalBranchName(dialog.targetBranch)) {
            Messages.showMessageDialog(project, "Target branch are not  allow equals local branch!!", localBranch, null)
            return false
        }
        val branchs = allDatas.mapNotNull { getRepo(it.title) }.mapNotNull { if (localBranch == it.currentBranchName) null else "${it.root.name}  ->  ${it.currentBranchName}" }
        if (branchs.isNotEmpty()) {
            Messages.showMessageDialog(project, "Those project branch are not equals local branch!! \n ${branchs.joinToString { it + "\n" }}", localBranch, null)
            return false
        }
        return true
    }
}