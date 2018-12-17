package com.pqixing.modularization.manager.tasks

import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.IdeUtils

/**
 * 切换分支
 */
open class PullProjectTask : BaseTask() {
    override fun runTask() {
        val fail = ArrayList<String>()
        val gits = ProjectManager.findAllGitPath().values.toMutableList()

        gits.forEach { if (!GitUtils.pull(ProjectManager.findGit(it.absolutePath))) fail.add(it.name) }
        IdeUtils.writeResult("PullProjectTask -> $fail", fail.size)
    }
}