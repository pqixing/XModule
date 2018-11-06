package com.pqixing.modularization.manager.tasks

import com.pqixing.modularization.Keys

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class UpdateCodeTask extends GitTask {
    @Override
    void start() {
        super.start()
        excludeGit.clear()
    }

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return Keys.TIP_GIT_NOT_EXISTS
        return com.pqixing.modularization.gradle.utils.GitUtils.run("manager pull origin", gitDir)
    }
}
