package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.git.execute
import com.pqixing.git.init
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.FileManager
import org.eclipse.jgit.api.Git

open class ToMavenTask : BaseTask() {
    init {
        this.dependsOn("uploadArchives", "ToMavenCheck", "CleanCache")
        project.getTasksByName("CleanCache", false)?.forEach { it.mustRunAfter("ToMavenCheck") }
        project.getTasksByName("uploadArchives", false)?.forEach { it.mustRunAfter("CleanCache") }
    }

    override fun start() {
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        val version = extHelper.getExtValue(project, Keys.LOG_VERSION)
        val branch = extHelper.getExtValue(project, Keys.LOG_BRANCH)
        val artifactId = extHelper.getExtValue(project, Keys.LOG_MODULE)

        val commitMsg = "${Keys.PREFIX_TO_MAVEN}?${Keys.LOG_BRANCH}=$branch&${Keys.LOG_MODULE}=$artifactId&${Keys.LOG_VERSION}=$version"
        val git = Git.open(project.rootDir)
        git.commit().setAllowEmpty(true).setMessage(commitMsg).init(FileManager.docCredentials).execute()
        git.push().init(FileManager.docCredentials).execute()
        Tools.println("ToMavenTask ${project.name} ->$commitMsg")
    }

    override fun runTask() {
    }

    override fun end() {
    }
}