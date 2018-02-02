package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.utils.TextUtils
import com.pqixing.modularization.wrapper.MetadataWrapper
import org.gradle.api.tasks.TaskAction

class ToMavenTask extends BaseTask {

    MavenType mavenInfo

    ToMavenTask() {
        project.afterEvaluate {}
    }

    @TaskAction
    void run() {
        checkVail()
        uploadFile()
        resetRepoVersionTime()
    }

    @Override
    void start() {
       String lastVersion =  MetadataWrapper.create(mavenInfo.maven_url,mavenInfo.groupName,wrapper.artifactId).release
        String lastBaseVersion = XmlUtils.parseLastBaseVersion(project)
        if (mavenInfo.focusUpload) {
            if(TextUtils.isEmpty(mavenInfo.pom_version)||mavenInfo.pom_version < lastBaseVersion) mavenInfo.pom_version = lastBaseVersion
            if(TextUtils.isEmpty(mavenInfo.updateDesc)) mavenInfo.updateDesc = "upload for batch"
            return
        }
        switch (mavenInfo.name) {
            case "release":
            case "test":
                if (TextUtils.isEmpty(mavenInfo.pom_version)) throw new RuntimeException("pom_version 为空,请填写版本号")
                if(mavenInfo.pom_version < lastBaseVersion) throw new RuntimeException("pom_version 不能小于仓库版本 $lastBaseVersion ,请重新配置,或强制上传")
//                if (NormalUtils.isEmpty(mavenInfo.updateDesc)) throw new RuntimeException("updateDesc 不能为空,请填写更新说明")
                break
        }
    }

    @Override
    void runTask() {

    }

    @Override
    void end() {

    }

    void resetRepoVersionTime() {

    }

    void checkVail() {

    }

//    @TaskAction
//    void refreshUploadProperties() {
//        def deployer = project.uploadArchives.repositories.mavenDeployer
//        def pom = deployer.pom
//        def repository = deployer.repository
//        repository.url = mavenInfo.maven_url
//        repository.authentication.userName = mavenInfo.userName
//        repository.authentication.password = mavenInfo.password
//        pom.groupId = mavenInfo.groupName + ".android"
//        pom.artifactId = mavenInfo.artifactId
//        pom.version = getVersion()
//        pom.name = mavenInfo.updateDesc
//        Print.l("pom.name :${pom.name} updateDesc :${mavenInfo.updateDesc} pros :${pom.properties.toSpreadMap()}")
//    }

    String getVersion() {
//        switch (mavenInfo.name) {
//            case "debug": return "${mavenInfo.pom_version}.${System.currentTimeMillis()}"
//            case "test":
        int lastVersion = 0
        try {
            lastVersion = XmlUtils.parseLastVersion(XmlUtils.getMetaUrl(mavenInfo.maven_url, mavenInfo.groupName, uploadArtifactId)).replace("${mavenInfo.pom_version}.", "").toInteger()
        } catch (Exception e) {
        }
        return "${mavenInfo.pom_version}.${lastVersion + 1}"
//            default: return mavenInfo.pom_version
//        }
    }

    String getUploadArtifactId() {
        return XmlUtils.getNameForBranch(project, project.name)
    }

    void uploadFile() {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository
        def version = getVersion()
        repository.url = mavenInfo.maven_url
        repository.authentication.userName = mavenInfo.userName
        repository.authentication.password = mavenInfo.password
        pom.groupId = mavenInfo.groupName + ".android"
        pom.artifactId = uploadArtifactId
        pom.version = version
        pom.name = "${System.currentTimeMillis()}##${mavenInfo.updateDesc} \n --- Git记录:$project.lastCommit"
        project.uploadArchives.execute()
        Print.lnf("uploadFile -> version :$pom.version artifactId : $pom.artifactId name = $pom.name url : $repository.url ")

        //上传完成以后,更新本地仓库中的版本
        File repoFile = new File(project.moduleConfig.buildConfig.defRepoPath)
        def pros = new Properties()
        if (repoFile.exists()) pros.load(repoFile.newInputStream())
        pros.put(uploadArtifactId, version)
        String timeStamp = "${uploadArtifactId}-stamp"
        pros.put(timeStamp, System.currentTimeMillis().toString())
        pros.store(repoFile.newOutputStream(), "")
    }
}