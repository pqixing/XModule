//package com.pqixing.modularization.maven
//
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.base.BaseTask
//import com.pqixing.modularization.git.GitConfig
//import com.pqixing.modularization.gradle.forOut.ProjectInfo
//import com.pqixing.modularization.modularization.Print
//import com.pqixing.modularization.wrapper.PomWrapper
//import org.gradle.api.GradleException
//
//class ToMavenTask extends BaseTask {
//
//    MavenType mavenInfo
//    GitConfig gitConfig
//
//    ToMavenTask() {
//        this.dependsOn "uploadArchives"
//        this.dependsOn "ToMavenCheck"
//        this.dependsOn "clean"
//
//        project.clean.mustRunAfter "ToMavenCheck"
//        project.uploadArchives.mustRunAfter "clean"
//    }
//
//    @Override
//    void start() {
//        mavenInfo = wrapper.getExtends(ProjectInfo).mavenType
//        gitConfig = wrapper.getExtends(GitConfig)
//    }
//
//    @Override
//    void runTask() {
//        Print.lnm("uploadArchives success -> version :$mavenInfo.pom_version artifactId : $mavenInfo.artifactId url : ${PomWrapper.getPomUrl(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId, mavenInfo.pom_version)} ")
//        Print.lnIde("$project.name${Keys.SEPERATOR}Y$Keys.SEPERATOR$mavenInfo.pom_version  uploadId -> $mavenInfo.artifactId")
//        Thread.sleep(1000)//睡眠两秒，再去更新
//        if (!com.pqixing.modularization.gradle.modularization.MavenUtils.updateMavenRecord(wrapper, mavenInfo.name, mavenInfo.maven_url, mavenInfo.artifactId)) {
//            throw new GradleException("Update Maven Record Fail!!!! Please Run Task UpdateMaven By Yours Self!! ")
//        }
//    }
//
//    @Override
//    void end() {
//    }
//}