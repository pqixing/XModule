package com.pqixing.modularization.maven

import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.android.pluginModule
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.modularization.utils.execute
import org.eclipse.jgit.api.Git

open class ToMavenTask : BaseTask() {
    val plugin = project.pluginModule()
    val vail = plugin.module.let { it.isAndroid&&!it.isApplication }
    init {
        if (vail) project.afterEvaluate{
            this.dependsOn("uploadArchives")
            project.getTasksByName("uploadArchives", false).forEach { it.dependsOn("ToMavenCheck") }
        }
    }

    override fun start() {
        if (!vail) {
            ResultUtils.writeResult("${plugin.module.getBranch()}:${project.name}:0.0")
            return
        }
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        val version = extHelper.getExtValue(project, Keys.LOG_VERSION)
        val branch = extHelper.getExtValue(project, Keys.LOG_BRANCH)
        val artifactId = extHelper.getExtValue(project, Keys.LOG_MODULE)

        val commitMsg = "${Keys.PREFIX_TO_MAVEN}?${Keys.LOG_BRANCH}=$branch&${Keys.LOG_MODULE}=$artifactId&${Keys.LOG_VERSION}=$version"
        val git = Git.open(project.getArgs().env.basicDir)
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