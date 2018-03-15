package com.pqixing.modularization.git

import com.pqixing.modularization.Keys

/**
 * Created by pqixing on 17-12-20.
 * 将本地所有工程切换到master分支
 */

class CheckMasterTask extends CheckBranchTask {
    CheckMasterTask() {
        group = Keys.GROUP_OTHER
    }

    @Override
    void start() {
        super.start()
        branchName = "master"
        target = "all"
    }
}
