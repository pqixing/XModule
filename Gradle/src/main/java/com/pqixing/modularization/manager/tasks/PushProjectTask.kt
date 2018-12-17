package com.pqixing.modularization.manager.tasks

import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.IdeUtils

/**
 * 切换分支
 */
open class PushProjectTask : BaseTask() {
    override fun runTask() {
        val fail = ArrayList<String>()
        val gits = ProjectManager.findAllGitPath().values.filter { it.exists() }.toMutableList()
        gits.add(ProjectManager.projectRoot)
        gits.forEach { if (it.exists() && !GitUtils.push(ProjectManager.findGit(it.absolutePath))) fail.add(it.name) }
        IdeUtils.writeResult("PushProjectTask -> $fail", fail.size)
    }
}