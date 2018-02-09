package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class PullTask extends GitTask {
    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        return GitUtils.run("git pull origin", gitDir)
    }
}
