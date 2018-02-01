package com.pqixing.modularization.git

import com.pqixing.modularization.base.BaseTask
/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class GitPullTask extends BaseTask {


    @Override
    void start() {

    }

    @Override
    void runTask() {
        "git pull".execute(null, project.projectDir)
    }

    @Override
    void end() {

    }
}
