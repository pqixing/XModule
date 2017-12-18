package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UploadTask extends DefaultTask {

    MavenType mavenInfo

    UploadTask() {
        group = Default.taskGroup
    }

    @TaskAction
    void refreshUploadProperties() {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository
        repository.url = mavenInfo.maven_url
        repository.authentication.userName = mavenInfo.userName
        repository.authentication.password = mavenInfo.password
        pom.groupId = mavenInfo.groupName + ".android"
        pom.artifactId = mavenInfo.artifactId
        pom.version = getVersion()
    }

    String getVersion() {
        switch (mavenInfo.name) {
            case "debug": return "${mavenInfo.pom_version}.${System.currentTimeMillis()}"
            case "test":
                def versionKey = "${mavenInfo.groupName}android:${mavenInfo.artifactId}:${mavenInfo.pom_version}".replace(".","-").replace(":","-")

                def configFile = new File(FileUtils.appendUrls(project.moduleConfig.buildConfig.rootPath, ".modularization"), "modularization.config")
                if (!configFile.exists()) {
                    configFile.parentFile.mkdirs()
                    configFile.createNewFile()
                }
                def versionPros = new Properties()
                versionPros.load(configFile.newInputStream())

                String version = versionPros.getProperty(versionKey)

                String newVersion = NormalUtils.isEmpty(version) ? "1" : (version.toInteger() + 1).toString()

                versionPros.setProperty(versionKey, newVersion)
                versionPros.store(configFile.newOutputStream(), "")
                return "${mavenInfo.pom_version}.${newVersion}"
            default: return mavenInfo.pom_version
        }
    }

    @TaskAction
    void uploadFile() {
        project.uploadArchives.execute()
    }
}