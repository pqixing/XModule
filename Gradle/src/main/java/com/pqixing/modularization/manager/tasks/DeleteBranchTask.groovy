package com.pqixing.modularization.manager.tasks

import com.pqixing.modularization.Keys

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class DeleteBranchTask extends GitTask {
    public DeleteBranchTask(){
        group = "others"
    }
    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return Keys.TIP_GIT_NOT_EXISTS
        if ("master" == branchName) return "-----"
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
        String result = ""

        if (hasLocal || hasRemote) {
            if (isCurBranch) {
                result += com.pqixing.modularization.gradle.utils.GitUtils.run("manager checkout master", gitDir)
            }
            result += com.pqixing.modularization.gradle.utils.GitUtils.run("manager branch -d ${hasLocal ? "" : "origin/"}${branchName}", gitDir)
            if (hasRemote) result += com.pqixing.modularization.gradle.utils.GitUtils.run("manager push origin :${branchName}", gitDir)
        } else {
            result += Keys.TIP_BRANCH_NOT_EXISTS
        }
        return result
    }
}
