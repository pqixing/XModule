package com.pqixing.modularization.maven

import com.pqixing.model.SubModuleType
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.modularization.utils.execute
import com.pqixing.modularization.utils.init
import org.eclipse.jgit.api.Git

open class ToMavenTask : BaseTask() {
    val plugin = AndroidPlugin.getPluginByProject(project)

    init {
        if (plugin.subModule.type != SubModuleType.TYPE_APPLICATION) {
            this.dependsOn("uploadArchives", "ToMavenCheck", "clean")
            project.getTasksByName("clean", false)?.forEach { it.mustRunAfter("ToMavenCheck") }
            project.getTasksByName("uploadArchives", false)?.forEach { it.mustRunAfter("clean") }
        }
    }

    override fun start() {
        if (plugin.subModule.type == SubModuleType.TYPE_APPLICATION) {
            ResultUtils.writeResult("${plugin.subModule.getBranch()}:${project.name}:0.0")
            return
        }
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        val version = extHelper.getExtValue(project, Keys.LOG_VERSION)
        val branch = extHelper.getExtValue(project, Keys.LOG_BRANCH)
        val artifactId = extHelper.getExtValue(project, Keys.LOG_MODULE)

        val commitMsg = "${Keys.PREFIX_TO_MAVEN}?${Keys.LOG_BRANCH}=$branch&${Keys.LOG_MODULE}=$artifactId&${Keys.LOG_VERSION}=$version"
        val git = Git.open(VersionManager.repoGitDir)
        GitUtils.pull(git)
        git.commit().setAllowEmpty(true).setMessage(commitMsg).init().execute()
        git.push().init().execute()
        ResultUtils.writeResult("$branch:$artifactId:$version")
    }

    override fun runTask() {
    }

    override fun end() {
    }
}