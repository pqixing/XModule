package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.git.GitUtils
import com.pqixing.git.execute
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.IdeUtils

open class CreateBranchTask : BaseTask() {
    override fun runTask() {
        val info = ManagerPlugin.getManagerPlugin().projectInfo

        var targetBranch = info.taskBranch
        if (targetBranch.isEmpty()) targetBranch = ProjectManager.rootBranch

        val gits = ProjectManager.findAllGitPath().values.toMutableList()
        gits.add(0, ProjectManager.projectRoot)
        gits.forEach {
            if (!GitUtils.checkIfClean(ProjectManager.findGit(it.absolutePath))) {
                Tools.printError("CreateBranchTask -> ${it.name}  checkIfClean :false, please check your file!")
            }
        }

        val fail = ArrayList<String>()
        gits.forEach {
            val create = GitUtils.createBranch(ProjectManager.findGit(it.absolutePath), targetBranch)
            if (!create) fail.add(it.name)
        }
        IdeUtils.writeResult("CreateBranchTask fail -> $fail", fail.size)
    }
}