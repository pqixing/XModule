package com.pqixing.modularization.common

import com.pqixing.modularization.base.BaseTask

/**
 * Created by pqixing on 17-12-22.
 */
class CleanCacheTask extends BaseTask {

    @Override
    void start() {
        try {
            project.clean.execute()
        } catch (Exception e) {
        }
    }

    @Override
    void runTask() {
        new File(wrapper.getExtends(BuildConfig).outDir).deleteDir()
    }

    @Override
    void end() {

    }
}
