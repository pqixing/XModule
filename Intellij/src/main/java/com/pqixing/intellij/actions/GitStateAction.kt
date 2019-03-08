package com.pqixing.intellij.actions

import com.intellij.openapi.vfs.VfsUtil
import com.pqixing.Templet.templet
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.adapter.JListSelectAdapter
import com.pqixing.intellij.adapter.JlistSelectListener
import com.pqixing.intellij.ui.GitOperatorDialog
import com.pqixing.intellij.utils.Git4IdeHelper
import git4idea.GitUtil
import git4idea.branch.GitBranchStateProvider
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRepository
import java.io.File
import javax.swing.JList

class GitStateAction : BaseGitAction, JlistSelectListener {
    override fun onItemSelect(jList: JList<*>, adapter: JListSelectAdapter, items: List<JListInfo>): Boolean {

        return true
    }
    constructor()
    constructor(repos: Map<String, GitRepository>) {
        allRepos.putAll(repos)
    }

    override fun checkUrls(urls: Map<String, String>): Boolean = true

    override fun initDialog(dialog: GitOperatorDialog) {
        dialog.setTargetBranch(null,false)
        dialog.jlTips.text = "Click item to resolve conflict"
        dialog.pOpertator.isVisible = false
        dialog.buttonOK.text = "Commit"
        dialog.adapter.boxVisible = false
        dialog.adapter.selectListener = this
    }

    override fun getAdapterList(urls: Map<String, String>): MutableList<JListInfo> {
        if (allRepos.isEmpty()) {
            allRepos.putAll(urls.filter { GitUtil.isGitRoot(it.key) }.map { Pair(it.key, Git4IdeHelper.getRepo(File(it.key), project)) })
            allRepos.put("$basePath/templet", Git4IdeHelper.getRepo(File(basePath, "templet"), project))
        }
        return allRepos.map {
            JListInfo(
                    title = VfsUtil.urlToPath(it.key),
                    log = it.value.currentBranchName ?: ""
            )
        }.toMutableList()
    }

    override fun checkOnOk(allDatas: MutableList<JListInfo>, dialog: GitOperatorDialog): Boolean = false

}