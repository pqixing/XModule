package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.MetadataWrapper

import java.util.regex.Pattern

class ToMavenTask extends BaseTask {

    MavenType mavenInfo

    @Override
    void start() {
        def dependent = wrapper.getExtends(Dependencies.class)
        if (dependent.hasLocalModule) {//如果有本地工程，抛异常
            throw new RuntimeException("current dependencies contain local project, please remove before upload : ${dependent.localImportModules.toString()}")
        }
        if (!CheckUtils.isEmpty(dependent.dependentLose)) {
            throw new RuntimeException("some dependencies lose, please import before upload : ${dependent.dependentLose.toString()}")
        }

        String baseVersion = MetadataWrapper.create(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId)
                .release.find(Pattern.compile("\\d*[.\\d*]"))
        if (!CheckUtils.isEmpty(baseVersion)) baseVersion = Keys.VERSION_DEFAULT //如果仓库没有版本，默认使用1.0
        if (!CheckUtils.isEmpty(mavenInfo.pom_version)) mavenInfo.pom_version = baseVersion

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
        pom.name = "${System.currentTimeMillis()}$Keys.SEPERATOR Git记录:${wrapper.getExtends(GitConfig).lastLog}"
    }

    @Override
    void end() {
        project.uploadArchives.execute()
        Print.lnf("uploadArchives success -> version :$mavenInfo.version artifactId : $mavenInfo.artifactId url : $mavenInfo.maven_url ")

        //上传完成以后,更新本地依赖版本信息
        File versionFile = wrapper.getExtends(Dependencies.class).versionFile
        def pros = FileUtils.readMaps(versionFile)
        String timeStamp = "$mavenInfo.artifactId$Keys.SUFFIX_STAMP"
        pros.put(timeStamp, System.currentTimeMillis().toString())
        pros.put(mavenInfo.artifactId, mavenInfo.pom_version)
        pros.store(versionFile.newOutputStream(), Keys.CHARSET)
        pros.clear()
    }

}