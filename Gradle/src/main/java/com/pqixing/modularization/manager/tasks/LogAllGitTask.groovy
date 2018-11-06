package com.pqixing.modularization.manager.tasks

import com.pqixing.modularization.Keys

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class LogAllGitTask extends GitTask {
    public LogAllGitTask(){
        group = "others"
    }
    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
     return Keys.TIP_GIT_NOT_EXISTS
    }
}
