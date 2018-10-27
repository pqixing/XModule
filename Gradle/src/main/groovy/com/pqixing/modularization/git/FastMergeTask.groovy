package com.pqixing.modularization.git

import com.pqixing.modularization.Keys
import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class FastMergeTask extends GitTask {

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return Keys.TIP_GIT_NOT_EXISTS
        boolean hasRemote = false
        boolean hasLocal = false
        boolean isCurBranch = false
        GitUtils.run("git branch -a ", gitDir, false)?.eachLine { line ->
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
        if (!hasRemote) return Keys.TIP_BRANCH_NOT_EXISTS
        GitUtils.run("git pull", gitDir)
        def mergeResult = GitUtils.run("git merge origin/$branchName  --ff-only", gitDir)?.trim() ?: ""
        if (mergeResult.startsWith("fatal:")) return Keys.TIP_GIT_MERGE_FAIL
        return GitUtils.run("git push", gitDir)

    }
}
