package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.gradle.common.GlobalConfig
import com.pqixing.modularization.net.Net

class VersionIndexTask extends BaseTask {
    @Override
    void start() {
        String groupUrl = com.pqixing.modularization.gradle.utils.TextUtils.getUrl(GlobalConfig.groupName)
        int length = groupUrl.length()
        GlobalConfig.preMavenUrl.each { mavenInfo ->

            def mavenStr = Net.get("$mavenInfo.value/$groupUrl")
//            if (!CheckUtils.isEmpty(mavenStr)) MavenUtils.clearVersionMaps(mavenInfo.key)
            mavenStr.eachLine { line ->
                int groupIndex = line.indexOf(groupUrl)
                if (groupIndex != -1 && line.contains("href=")) {
                    String artifactId = line.substring(groupIndex + length + 1, line.indexOf("/\">"))
                    com.pqixing.modularization.gradle.utils.MavenUtils.updateMavenRecord(wrapper, mavenInfo.key, mavenInfo.value, artifactId, false)
                }
            }
        }
        com.pqixing.modularization.gradle.utils.MavenUtils.pushMaven()
    }

    @Override
    void runTask() {
    }

    @Override
    void end() {
    }

}