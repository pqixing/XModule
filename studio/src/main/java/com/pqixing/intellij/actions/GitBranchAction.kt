package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.group.QToolGroup
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.GitHelper
import git4idea.GitUtil
import java.io.File

class GitBranchAction : BaseGitAction() {
    override fun update(e: AnActionEvent) {
        e?.presentation?.isEnabledAndVisible = QToolGroup.hasBasic(e?.project)
    }

    override fun beforeActionRun() = key == Messages.showPasswordDialog("Please input key!!", "Password")
    override fun checkUrls(urls: Map<String, String>): Boolean {
        val filter = urls.keys.filter { !GitUtil.isGitRoot(File(it)) }
        if (filter.isEmpty()) return true
        val exitCode = Messages.showYesNoCancelDialog("The following projects are not clone,It may lead to unpredictable exception !!!\n${filter.joinToString { it + "\n" }}", "Miss Project", "ToMerge", "ToClone", "Cancel", null)
        if (exitCode == Messages.YES) return true
        if (exitCode == Messages.NO) ApplicationManager.getApplication().invokeLater {
            GitSyncAction().actionPerformed(e)
        }
        return false
    }

    override fun initDialog(dialog: GitOperatorDialog) {
        dialog.setOperator("create", "merge", "delete")
        dialog.setTargetBranch(getRepo(rootRepoPath)?.branches?.remoteBranches?.map { it.name }, true)
        dialog.setOnOperatorChange { }//重新设置，不需要更新状态
    }

    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean {
        val localBranch = getRepo(rootRepoPath)?.currentBranchName
        if (localBranch == null) {
            Messages.showMessageDialog(project, "Unable to find local branch root project!!", "Miss Branch", null)
            return false
        }
        val operatorCmd = dialog.operatorCmd
        //不允许操作master
        if ("delete" == operatorCmd && "master" == GitHelper.findLocalBranchName(dialog.targetBranch)) {
            Messages.showErrorDialog(project, "Unable to delete branch : master", "Error")
            return false
        }
        if (localBranch == GitHelper.findLocalBranchName(dialog.targetBranch)) {
            Messages.showMessageDialog(project, "Target branch is not allowed to be the same as local branch", localBranch, null)
            return false
        }
        val branchs = allDatas.filter { it.select }.mapNotNull { getRepo(it.title) }.mapNotNull { if (localBranch == it.currentBranchName) null else "${it.root.name}  ->  ${it.currentBranchName}" }
        if (branchs.isNotEmpty()) {
            val exitCode = Messages.showOkCancelDialog(project, "Those project branch are not equals local branch!! \n ${branchs.joinToString { it + "\n" }}", localBranch, "ToCheckOut", "Cancel", null)
            if (exitCode == Messages.OK) {
                dialog.dispose()
                ApplicationManager.getApplication().invokeLater { GitCheckoutAction().actionPerformed(e) }
            }
            return false
        }
        return true
    }
}