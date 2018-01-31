package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UploadTask extends DefaultTask {

    MavenType mavenInfo

    UploadTask() {
        group = Default.taskGroup

    }

    @TaskAction
    void run() {
        checkVail()
        uploadFile()
        resetRepoVersionTime()
    }

    void resetRepoVersionTime() {

    }

    void checkVail() {
        String lastBaseVersion = NormalUtils.parseLastBaseVersion(project)
        if (mavenInfo.focusUpload) {
            if(NormalUtils.isEmpty(mavenInfo.pom_version)||mavenInfo.pom_version < lastBaseVersion) mavenInfo.pom_version = lastBaseVersion
            if(NormalUtils.isEmpty(mavenInfo.updateDesc)) mavenInfo.updateDesc = "upload for batch"
            return
        }
        switch (mavenInfo.name) {
            case "release":
            case "test":
                if (NormalUtils.isEmpty(mavenInfo.pom_version)) throw new RuntimeException("pom_version 为空,请填写版本号")
                if(mavenInfo.pom_version < lastBaseVersion) throw new RuntimeException("pom_version 不能小于仓库版本 $lastBaseVersion ,请重新配置,或强制上传")
//                if (NormalUtils.isEmpty(mavenInfo.updateDesc)) throw new RuntimeException("updateDesc 不能为空,请填写更新说明")
                break
        }
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
            lastVersion = NormalUtils.parseLastVersion(NormalUtils.getMetaUrl(mavenInfo.maven_url, mavenInfo.groupName, uploadArtifactId)).replace("${mavenInfo.pom_version}.", "").toInteger()
        } catch (Exception e) {
        }
        return "${mavenInfo.pom_version}.${lastVersion + 1}"
//            default: return mavenInfo.pom_version
//        }
    }

    String getUploadArtifactId() {
        return NormalUtils.getNameForBranch(project, project.name)
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