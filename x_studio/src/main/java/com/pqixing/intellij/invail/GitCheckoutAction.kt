//package com.pqixing.intellij.invail
//
//import com.intellij.openapi.actionSystem.AnActionEvent
//import com.pqixing.intellij.ui.adapter.JListInfo
//import com.pqixing.intellij.common.XGroup
//import com.pqixing.intellij.git.uitils.GitHelper
//import git4idea.GitUtil
//import java.io.File
//
//class GitCheckoutAction : BaseGitAction() {
//
//    override fun checkUrls(urls: Map<String, String>): Boolean = true
//    override fun update(e: AnActionEvent) {
//        e?.presentation?.isEnabledAndVisible = XGroup.isBasic(e?.project)
//    }
//    override fun initDialog(dialog: GitOperatorDialog) {
//        dialog.setTargetBranch(getRepo(rootRepoPath)?.branches?.remoteBranches?.map { it.name.substring(it.name.lastIndexOf("/") + 1) }, true)
//        dialog.pOpertator.isVisible = false
//    }
//
//    override fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> = super.getAdapterList(urls.filter { GitUtil.isGitRoot(File(it.key)) })
//
//    override fun updateItemLog(it: JListInfo, operatorCmd: String, cacheLog: String?) {
//        it.log = cacheLog ?: getRepo(it.title)?.currentBranchName ?: ""
//    }
//
//    override fun doOk(dialog: GitOperatorDialog, allDatas: MutableList<JListInfo>, urls: Map<String, String>, rootBranch: String?) {
////        if (rootBranch == dialog.targetBranch) {
////            dialog.dispose()
////            Messages.showMessageDialog(project, "Target branch can not equals local branch!!", dialog.targetBranch, null)
////            return
////        }
//        dialog.dispose()
//        val selectItem = allDatas.filter { it.select }.map { it.title }
//        val selectRepo = allRepos.filter { selectItem.contains(it.key) }
//        //切换完成,更新状态
//        GitHelper.checkout(project, dialog.targetBranch, selectRepo.values.toList()) {
//            GitStateAction(selectRepo).actionPerformed(e)
//            afterDoOk(dialog)
//        }
//    }
//
//    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean = true
//}