package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CheckBranchTask extends GitTask {

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return "git do not exists"

        boolean hasLocal = false
        boolean isCurBranch = false
        GitUtils.run("git branch", gitDir,false)?.eachLine { line ->
            line = line.trim()
            if (line.endsWith(branchName)) {
                hasLocal = true
                isCurBranch = line != branchName
            }
        }
        if (isCurBranch) return "current branch is $branchName"
        if (hasLocal) return GitUtils.run("git checkout $branchName", gitDir)

        return GitUtils.run("git checkout -b $branchName origin/$branchName", gitDir)
        +"\nAfter ->" + GitUtils.run("git rev-parse --abbrev-ref HEAD", gitDir)
    }
}
