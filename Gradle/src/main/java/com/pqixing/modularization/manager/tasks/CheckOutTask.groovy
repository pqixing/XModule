package com.pqixing.modularization.manager.tasks

import com.pqixing.modularization.Keys

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CheckOutTask extends GitTask {

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return Keys.TIP_GIT_NOT_EXISTS
        boolean hasRemote = false
        boolean hasLocal = false
        boolean isCurBranch = false
        com.pqixing.modularization.gradle.utils.GitUtils.run("manager branch -a ", gitDir, false)?.eachLine { line ->
            line = line.trim()
            if (line.contains("/origin/$branchName")) {
                hasRemote = true
            }
            if (!line.contains("/origin/") && line.endsWith(branchName)) {
                hasLocal = true
                isCurBranch = line != branchName
            }
        }
        if (isCurBranch) return "current branch is $branchName"
        if (hasLocal) return com.pqixing.modularization.gradle.utils.GitUtils.run("manager checkout $branchName", gitDir)
        if (hasRemote) return com.pqixing.modularization.gradle.utils.GitUtils.run("manager checkout -b $branchName origin/$branchName", gitDir)

        return Keys.TIP_BRANCH_NOT_EXISTS
    }
}
