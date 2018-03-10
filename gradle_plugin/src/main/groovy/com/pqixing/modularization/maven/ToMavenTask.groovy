package com.pqixing.modularization.maven

import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.MavenUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.PomWrapper
import org.gradle.api.GradleException

class ToMavenTask extends BaseTask {

    MavenType mavenInfo
    GitConfig gitConfig

    ToMavenTask() {
        this.dependsOn "uploadArchives"
        this.dependsOn "ToMavenCheck"
        project.uploadArchives.mustRunAfter "ToMavenCheck"
    }

    @Override
    void start() {
        mavenInfo = wrapper.getExtends(ModuleConfig).mavenType
        gitConfig = wrapper.getExtends(GitConfig)
    }

    @Override
    void runTask() {
        Print.lnm("uploadArchives success -> version :$mavenInfo.pom_version artifactId : $mavenInfo.artifactId url : ${PomWrapper.getPomUrl(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId, mavenInfo.pom_version)} ")
        Thread.sleep("2000")//睡眠两秒，再去更新
        if (!MavenUtils.updateMavenRecord(wrapper, mavenInfo.name, mavenInfo.maven_url, mavenInfo.artifactId)) {
            throw new GradleException("Update Maven Record Fail!!!! Please Run Task UpdateMaven By Yours Self!! ")
        }
    }

    @Override
    void end() {
    }
}