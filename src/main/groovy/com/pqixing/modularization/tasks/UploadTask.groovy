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
    }

    void checkVail() {
        switch (mavenInfo.name) {
            case "release":
            case "test":
                if (NormalUtils.isEmpty(mavenInfo.updateDesc)) throw new RuntimeException("lose update log ,please config updateDesc")
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
                    lastVersion = NormalUtils.parseLastVersion(NormalUtils.getMetaUrl(mavenInfo.maven_url, mavenInfo.groupName, mavenInfo.artifactId)).replace("${mavenInfo.pom_version}.", "").toInteger()
                } catch (Exception e) {
                }
                return "${mavenInfo.pom_version}.${lastVersion + 1}"
//            default: return mavenInfo.pom_version
//        }
    }

    void uploadFile() {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository
        repository.url = mavenInfo.maven_url
        repository.authentication.userName = mavenInfo.userName
        repository.authentication.password = mavenInfo.password
        pom.groupId = mavenInfo.groupName + ".android"
        pom.artifactId = mavenInfo.artifactId
        pom.version = getVersion()
        pom.name = "${System.currentTimeMillis()}## $mavenInfo.updateDesc"
        project.uploadArchives.execute()
        Print.lnf("uploadFile -> url : $repository.url version :$pom.version artifactId : $pom.artifactId name = $pom.name")
    }
}