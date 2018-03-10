package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.MetadataWrapper
import com.pqixing.modularization.wrapper.PomWrapper
import org.gradle.api.GradleException

class ToMavenCheckTask extends BaseTask {

    MavenType mavenInfo
    GitConfig gitConfig

    @Override
    void start() {
        mavenInfo = wrapper.getExtends(ModuleConfig).mavenType
        gitConfig = wrapper.getExtends(GitConfig)

        Print.lnm("$project.name start upload env : $mavenInfo.name artifactId: ${wrapper.artifactId}")

        String errorMsg = ""
        if (wrapper.pluginName != Keys.NAME_LIBRARY) {
            errorMsg = "current plugin is ${wrapper.pluginName} ,can not upload to maven !!!!!"
        }
        def dependent = wrapper.getExtends(Dependencies.class)
        if (dependent.hasLocalModule) {//如果有本地工程，抛异常
            errorMsg = "current dependencies contain local project, please remove before upload : ${dependent.localDependency.toString()}"
        }
        if (!CheckUtils.isEmpty(dependent.dependentLose)) {
            errorMsg = "some dependencies lose, please import before upload : ${dependent.dependentLose.toString()}"
        }
        String lastRelease = MetadataWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId).release.trim()
        if (CheckUtils.isEmpty(lastRelease)) lastRelease = Keys.VERSION_DEFAULT //如果仓库没有版本，默认使用1.0.0

        PomWrapper pomWrapper = PomWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId, lastRelease)
        if (gitConfig.revisionNum == pomWrapper.revisionNum) {//如果本地的git版本号等于仓库最后一次提交的版本号，则不上传
            errorMsg = "----->  Not Update : -->     current git num equals maven revision,${gitConfig.revisionNum}"
        }

        if (!CheckUtils.isEmpty(errorMsg)) {
            errorMsg = ":$project.name " + errorMsg
            Print.lnm(errorMsg)
            throw new GradleException(errorMsg)
        }

        int lastPoint = lastRelease.lastIndexOf(".")
        String baseVersion = lastRelease.substring(0, lastPoint)
        int lastVersion = lastRelease.substring(lastPoint + 1).toInteger() + 1
        lastRelease = "${baseVersion}.${lastVersion}"

        if (CheckUtils.isEmpty(mavenInfo.pom_version)) mavenInfo.pom_version = baseVersion

        if (mavenInfo.pom_version < baseVersion) {
            if (mavenInfo.focusUpload) mavenInfo.pom_version = lastRelease
            else {
                Print.lnm("pom_version error  cur ${mavenInfo.pom_version} maven : $baseVersion ")
                throw new GradleException("pom_version error  cur ${mavenInfo.pom_version} maven : $baseVersion ")
            }
        } else {
            mavenInfo.pom_version = "${mavenInfo.pom_version}.$lastVersion"
        }
    }

    @Override
    void runTask() {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository

        repository.url = mavenInfo.maven_url
        repository.authentication.userName = mavenInfo.userName
        repository.authentication.password = mavenInfo.password
        pom.groupId = mavenInfo.groupName
        pom.artifactId = mavenInfo.artifactId
        pom.version = mavenInfo.pom_version
        pom.name = "${System.currentTimeMillis()}${Keys.SEPERATOR}${wrapper.getExtends(GitConfig).revisionNum}$Keys.SEPERATOR-Git记录 ${gitConfig.branchName}:${gitConfig.lastLog}"
        Print.lnm("uploadArchives -> pom: $pom")
    }

    @Override
    void end() {

    }
}