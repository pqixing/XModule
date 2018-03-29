package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils
/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CreateBranchTask extends GitTask {
    CreateBranchTask(){
        this.dependsOn "CreateBranchFocusVersions"
        this.dependsOn "GitUpdate"
        this.dependsOn "GitUpdate"
        this.dependsOn "GitCloneAll"
    }

    @Override
    void start() {
        super.start()
        target = "all"
    }

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return "git do not existes"
        boolean hasRemote = false
        boolean hasLocal = false
        boolean isCurBranch = false
        GitUtils.run("git branch -a ", gitDir,false)?.eachLine { line ->
            line = line.trim()
            if(line.contains("/origin/$branchName")){
                hasRemote = true
            }
            if(!line.contains("/origin/") && line.endsWith(branchName)){
                hasLocal = true
                isCurBranch = line != branchName
            }
        }
        if(isCurBranch) return "current branch is $branchName"
        if(hasLocal) return GitUtils.run("git checkout $branchName", gitDir)
        if(hasRemote) return GitUtils.run("git checkout -b $branchName /origin/$branchName", gitDir)
        return GitUtils.run("git checkout -b $branchName", gitDir)+"\n"+GitUtils.run("git push origin $branchName", gitDir)
    }
}
