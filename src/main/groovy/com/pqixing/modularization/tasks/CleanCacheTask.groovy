package com.pqixing.modularization.tasks

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.configs.BuildConfig

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
