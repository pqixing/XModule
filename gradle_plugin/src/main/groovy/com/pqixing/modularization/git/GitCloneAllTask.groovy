package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class GitCloneAllTask extends GitTask {

    @Override
    void start() {
        super.start()
        target = "all"
    }

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (gitDir.exists()) return "----already exists"
        String result = GitUtils.run("git clone $gitUrl", gitDir.parentFile)
        result += GitUtils.run("git checkout -b ${branchName} origin/${branchName}", gitDir)
        return result
    }
}
