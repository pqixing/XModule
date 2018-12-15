package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.IdeUtils
import com.pqixing.tools.FileUtils

open class CloneProjectTask : BaseTask() {

    val clones = ArrayList<String>()
    val fails = ArrayList<String>()

    override fun runTask() = ProjectManager.findAllGitPath().forEach {
        val dir = it.value
        val gitUrl = it.key
        if (GitUtils.isGitDir(dir)) {
            Tools.println("          -> ${dir.name} exists")
            return@forEach
        }
        if (dir.exists()) {
            FileUtils.delete(dir)
        }
        Tools.println("          start clone-> ${dir.name} $gitUrl")
        val git = GitUtils.clone(gitUrl, dir, ProjectManager.rootBranch)
        ProjectManager.setGit(dir.absolutePath, git)
        (if (GitUtils.isGitDir(dir)) clones else fails).add("\n${dir.name}:$gitUrl")
    }

    override fun end() {
        val result = "Clone -> $clones,Fail -> $fails"
        IdeUtils.writeResult(result, fails.size)
    }
}