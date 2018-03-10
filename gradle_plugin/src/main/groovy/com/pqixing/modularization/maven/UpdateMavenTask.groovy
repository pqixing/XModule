package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.MavenUtils

class UpdateMavenTask extends BaseTask {
    @Override
    void start() {
        GlobalConfig.preMavenUrl.each { mavenInfo ->
            MavenUtils.getMavenMaps(mavenInfo.key).each { map ->
                MavenUtils.updateMavenRecord(wrapper, mavenInfo.key,mavenInfo.value, map.key)
            }
        }
    }

    @Override
    void runTask() {
    }

    @Override
    void end() {
    }
}