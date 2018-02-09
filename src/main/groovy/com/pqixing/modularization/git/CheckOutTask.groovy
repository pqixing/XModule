package com.pqixing.modularization.git

import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CheckOutTask extends GitTask {

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (!gitDir.exists()) return "git do not existes"
        String result = ""
        result += GitUtils.run("git stash save -a 'messeag'", gitDir)
        result += GitUtils.run("git branch -D $gitConfig.checkout", gitDir)
        result += GitUtils.run("git checkout -b $gitConfig.checkout origin/$gitConfig.checkout", gitDir)
        result += GitUtils.run("git stash pop", gitDir)
        return result
    }
}
