package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.gradle.forOut.ProjectInfo
import com.pqixing.modularization.gradle.common.GlobalConfig
import com.pqixing.modularization.gradle.dependent.Dependencies
import org.gradle.api.GradleException

class ToMavenCheckTask extends BaseTask {

    MavenType mavenInfo
    GitConfig gitConfig

    ToMavenCheckTask() {
        group = ""
    }

    @Override
    void start() {
        mavenInfo = wrapper.getExtends(ProjectInfo).mavenType
        gitConfig = wrapper.getExtends(GitConfig)

        com.pqixing.modularization.gradle.utils.Print.lnm("$project.name start upload env : $mavenInfo.name artifactId: ${wrapper.artifactId}")

        String errorMsg = ""
        if (wrapper.pluginName != Keys.NAME_LIBRARY) {
            errorMsg = "current plugin is ${wrapper.pluginName} ,can not upload to maven !!!!!"
        }
        if (!GlobalConfig.gitLog) {
            errorMsg = "gitLog is close ,please open it before upload !!!!!"
        }

//        errorMsg += docUpdateLog()

        def dependent = wrapper.getExtends(Dependencies.class)
        if (dependent.hasLocalModule) {//如果有本地工程，抛异常
            errorMsg = "Contain local project, please remove before upload : ${dependent.localDependency.toString()}"
        }
        if (!com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(dependent.dependentLose)) {
            errorMsg = "Lose dependency, please import them before upload : ${dependent.dependentLose.toString()}"
        }
        String lastRelease = com.pqixing.modularization.gradle.wrapper.MetadataWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId).release.trim()
        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(lastRelease)) lastRelease = Keys.VERSION_DEFAULT //如果仓库没有版本，默认使用1.0.0

        com.pqixing.modularization.gradle.wrapper.PomWrapper pomWrapper = com.pqixing.modularization.gradle.wrapper.PomWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId, lastRelease)
        if (gitConfig.revisionNum == pomWrapper.revisionNum) {//如果本地的git版本号等于仓库最后一次提交的版本号，则不上传
            errorMsg = "::Not Update:: -> revisionNum=${gitConfig.revisionNum}::version=$lastRelease"
        }
        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(errorMsg) && hasUpdate()) {
            errorMsg = "Local code is different from remote,Please update your code or Check whether it needs to be push"
        }

        if (!com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(errorMsg)) {
            com.pqixing.modularization.gradle.utils.Print.lnIde("$project.name${Keys.SEPERATOR}N$Keys.SEPERATOR$errorMsg")

            errorMsg = ":$project.name " + errorMsg
            com.pqixing.modularization.gradle.utils.Print.lnm(errorMsg)
            throw new GradleException(errorMsg)
        }

        int lastPoint = lastRelease.lastIndexOf(".")
        String baseVersion = lastRelease.substring(0, lastPoint)
        int lastVersion = lastRelease.substring(lastPoint + 1).toInteger() + 1
        lastRelease = "${baseVersion}.${lastVersion}"

        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(mavenInfo.pom_version)) mavenInfo.pom_version = baseVersion

        if (mavenInfo.pom_version < baseVersion) {
            if (mavenInfo.focusUpload) mavenInfo.pom_version = lastRelease
            else {
                com.pqixing.modularization.gradle.utils.Print.lnm("Pom_version error  cur ${mavenInfo.pom_version} maven : $baseVersion ")
                com.pqixing.modularization.gradle.utils.Print.lnIde("$project.name${Keys.SEPERATOR}N$Keys.SEPERATOR$errorMsg")
                throw new GradleException("pom_version error  cur ${mavenInfo.pom_version} maven : $baseVersion ")
            }
        } else {
            mavenInfo.pom_version = "${mavenInfo.pom_version}.$lastVersion"
        }
    }
    /**
     * 判断当前代码于远程代码是否一致
     * @return
     */
    boolean hasUpdate() {
        //如果配置了不检查代码更新,则不读取版本号
        if(!GlobalConfig.checkCodeUpdate) return false

        return gitConfig.revisionNum != com.pqixing.modularization.gradle.utils.TextUtils.removeMark(com.pqixing.modularization.gradle.utils.GitUtils.run("manager println -1 --pretty=format:'%H' origin/${gitConfig.branchName} ${gitConfig.rootForGit ? '' : (project.name + '/')}", gitConfig.gitDir))
    }
    /**
     * 检查Docment是否需要更新
     * @return
     */
    String docUpdateLog() {
        def documentDir = com.pqixing.modularization.gradle.utils.MavenUtils.documentDir

        def gitInfo = com.pqixing.modularization.gradle.utils.GitUtils.run("manager println -1 HEAD --oneline --pretty=format:'%H::%D'", documentDir).trim().split("::")
        String revisionNum = ""
        String branchName = ""
        if (gitInfo.length > 0)
            revisionNum = gitInfo[0]
        if (gitInfo.length > 1)
            branchName = gitInfo[1].split(",")[0].replace("HEAD", "").replace("->", "").trim()
        if ("%D" == branchName) {
            branchName = com.pqixing.modularization.gradle.utils.GitUtils.run("manager rev-parse --abbrev-ref HEAD", documentDir).trim()
        }
        if (branchName != "master") return "Document manager current branch is $config.branchName , please checkout to master before upload!!!"

        def remoteLog = com.pqixing.modularization.gradle.utils.GitUtils.run("manager ls-remote ", documentDir).readLines().find { it.endsWith("/master") }
//        Print.lnf("remoteLog $remoteLog branchName $branchName revisionNum $revisionNum")

        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(remoteLog)) return ""
        def remoteRevisionNum = remoteLog.substring(0, remoteLog.indexOf("\t")).trim()
        if (remoteRevisionNum != revisionNum) return "Document manager has update , please update before upload!!!"
        return ""
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
        com.pqixing.modularization.gradle.utils.Print.lnm("uploadArchives -> pom: $pom")
    }

    @Override
    void end() {

    }
}