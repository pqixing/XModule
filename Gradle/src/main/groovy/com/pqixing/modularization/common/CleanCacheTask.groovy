package com.pqixing.modularization.common

import com.pqixing.modularization.base.BaseTask

/**
 * Created by pqixing on 17-12-22.
 */
class CleanCacheTask extends BaseTask {
    public CleanCacheTask(){
        this.dependsOn "clean"
    }

    @Override
    void start() {
    }

    @Override
    void runTask() {
        new File(wrapper.getExtends(BuildConfig).outDir).deleteDir()
    }

    @Override
    void end() {

    }
}
