package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import java.io.File

open class CloneProjectTask : BaseTask() {

    val clones = ArrayList<String>()
    val fails = ArrayList<String>()
    val exists = ArrayList<String>()

    override fun runTask() = project.getArgs().projectXml.projects.forEach {
        val dir = File(project.getArgs().env.codeRootDir, it.name)
        if (GitUtils.isGitDir(dir)) {
            exists.add(dir.name)
            return@forEach
        }
        if (dir.exists()) {
            FileUtils.delete(dir)
        }
        Tools.println("          start clone-> ${dir.name} ${it.url}")
        GitUtils.clone(it.url, dir, project.getArgs().env.templetBranch)?.close()
        (if (GitUtils.isGitDir(dir)) clones else fails).add("\n${dir.name}:${it.url}")
    }

    override fun end() {
        val result = "Clone -> $clones,  Fail -> $fails  exists -> $exists"
        ResultUtils.writeResult(result, fails.size)
    }
}