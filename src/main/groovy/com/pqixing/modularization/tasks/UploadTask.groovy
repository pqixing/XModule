package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.MavenType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.pqixing.modularization.utils.Print

class UploadTask extends DefaultTask {

    MavenType mavenInfo

    UploadTask() {
        group = Default.taskGroup
    }

    @TaskAction
    void upadteMavenInfo() {
        Print.ln("UploadTask name = $mavenInfo.name")
        mavenInfo.generatorFiles()
    }

    @TaskAction
    void generatorMavenFile() {

    }

    @TaskAction
    void uploadFile() {

    }

    @TaskAction
    void deleteMavenFile() {

    }
}