package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.PomWrapper

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

        //上传完成以后,更新本地依赖版本信息
        File versionFile = wrapper.getExtends(Dependencies.class).versionFile
        versionFile.parentFile.mkdirs()
        def pros = FileUtils.readMaps(versionFile)
        String timeStamp = "$mavenInfo.artifactId$Keys.SUFFIX_STAMP"
        pros.put(timeStamp, System.currentTimeMillis().toString())
        pros.put(mavenInfo.artifactId, mavenInfo.pom_version)
        pros.store(versionFile.newOutputStream(), Keys.CHARSET)
        pros.clear()
    }

    @Override
    void end() {
    }
}