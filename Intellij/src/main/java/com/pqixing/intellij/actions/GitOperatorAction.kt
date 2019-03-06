package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.utils.Git4IdeHelper
import git4idea.GitUtil
import git4idea.actions.GitFetch
import git4idea.commands.GitCommandResult
import git4idea.repo.GitRepository

class GitOperatorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

//        Git4IdeHelper.getGit().fetch()
//        Git4IdeHelper.getGit().getUnmergedFiles()
//        val repo: GitCommandResult




    }
}