package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.MetadataWrapper

class ToMavenTask extends BaseTask {

    MavenType mavenInfo

    @Override
    void start() {
        if (wrapper.pluginName != Keys.NAME_LIBRARY) {
            throw new RuntimeException("current plugin is ${wrapper.pluginName} ,can not upload to maven !!!!!")
        }
        def dependent = wrapper.getExtends(Dependencies.class)
        if (dependent.hasLocalModule) {//如果有本地工程，抛异常
            throw new RuntimeException("${wrapper.project.name} : current dependencies contain local project, please remove before upload : ${dependent.localDependency.toString()}")
        }
        if (!CheckUtils.isEmpty(dependent.dependentLose)) {
            throw new RuntimeException("some dependencies lose, please import before upload : ${dependent.dependentLose.toString()}")
        }
        String lastRelease = MetadataWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId).release.trim()
        if (CheckUtils.isEmpty(lastRelease)) lastRelease = Keys.VERSION_DEFAULT //如果仓库没有版本，默认使用1.0
        int lastPoint = lastRelease.lastIndexOf(".")
        String baseVersion = lastRelease.substring(0, lastPoint)
        int lastVersion = lastRelease.substring(lastPoint + 1).toInteger() + 1

        if (!CheckUtils.isEmpty(mavenInfo.pom_version)) mavenInfo.pom_version = baseVersion
        mavenInfo.pom_version += ".$lastVersion"

        if (mavenInfo.pom_version < baseVersion) {
            if (mavenInfo.focusUpload) mavenInfo.pom_version = baseVersion
            else throw new RuntimeException("pom_version can not less than maven version : $baseVersion --------------")
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
        pom.name = "${System.currentTimeMillis()}$Keys.SEPERATOR-Git记录:${wrapper.getExtends(GitConfig).lastLog}-num:${wrapper.getExtends(GitConfig).revisionNum}"
    }

    @Override
    void end() {
        project.uploadArchives.execute()
        Print.lnf("uploadArchives success -> version :$mavenInfo.pom_version artifactId : $mavenInfo.artifactId url : $mavenInfo.maven_url ")

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

}