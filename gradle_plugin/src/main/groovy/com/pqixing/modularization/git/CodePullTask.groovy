package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CodePullTask extends GitTask {
    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return "git do not exists"
        return GitUtils.run("git pull origin", gitDir)
    }
}
