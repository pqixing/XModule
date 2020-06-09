package com.pqixing.modularization.maven

import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.IExtHelper
import com.pqixing.modularization.android.MDPlugin
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.modularization.utils.execute
import org.eclipse.jgit.api.Git

open class ToMavenTask : BaseTask() {
    val plugin = project.MDPlugin()

    init {
        if (!plugin.subModule.isApplication) {
            this.dependsOn("uploadArchives")
            project.getTasksByName("uploadArchives", false).forEach { it.dependsOn("ToMavenCheck") }
        }
    }

    override fun start() {
        if (plugin.subModule.isApplication) {
            ResultUtils.writeResult("${plugin.subModule.getBranch()}:${project.name}:0.0")
            return
        }
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        val version = extHelper.getExtValue(project, Keys.LOG_VERSION)
        val branch = extHelper.getExtValue(project, Keys.LOG_BRANCH)
        val artifactId = extHelper.getExtValue(project, Keys.LOG_MODULE)

        val commitMsg = "${Keys.PREFIX_TO_MAVEN}?${Keys.LOG_BRANCH}=$branch&${Keys.LOG_MODULE}=$artifactId&${Keys.LOG_VERSION}=$version"
        val git = Git.open(project.getArgs().env.templetRoot)
        GitUtils.pull(git)
        git.commit().setAllowEmpty(true).setMessage(commitMsg).execute()
        git.push().execute()
        ResultUtils.writeResult("$branch:$artifactId:$version")
    }

    override fun runTask() {
    }

    override fun end() {
    }
}