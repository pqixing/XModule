package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask

open class ToMavenTask : BaseTask() {
    init {
        this.dependsOn("uploadArchives", "ToMavenCheck", "clean")
        project.getTasksByName("clean", false)?.forEach { it.mustRunAfter("ToMavenCheck") }
        project.getTasksByName("uploadArchives", false)?.forEach { it.mustRunAfter("clean") }
    }

    override fun start() {
        //        this.dependsOn "uploadArchives"
//        this.dependsOn "ToMavenCheck"
//        this.dependsOn "clean"
//
//        project.clean.mustRunAfter "ToMavenCheck"
//        project.uploadArchives.mustRunAfter "clean"
    }

    override fun runTask() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun end() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}