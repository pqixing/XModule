package com.pqixing.modularization.manager.tasks


import com.pqixing.modularization.gradle.common.GlobalConfig

/**
 * Created by pqixing on 17-12-20.
 * 创建分支的依赖tag
 */

class VersionTagTask extends GitTask {
    VersionTagTask() {
        group = "other"
    }

    @Override
    void end() {
        com.pqixing.modularization.gradle.utils.MavenUtils.upDocumentDir
        GlobalConfig.preMavenUrl.each {
            com.pqixing.modularization.gradle.utils.MavenUtils.saveMavenMaps(it.key, branchName, target == "all" ? "all" : branchName)
        }
        com.pqixing.modularization.gradle.utils.MavenUtils.pushMaven()
    }

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        return ""
    }
}
