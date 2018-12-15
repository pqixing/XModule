package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.IdeUtils

/**
 * 切换分支
 */
open class CheckOutTask : BaseTask() {
    override fun runTask() {
        val info = ManagerPlugin.getManagerPlugin().projectInfo
        var targetBranch = info.taskBranch
        if (targetBranch.isEmpty()) targetBranch = ProjectManager.rootBranch

        val fail = ArrayList<String>()
        val gits = ProjectManager.findAllGitPath().values.toMutableList()
        //判断rootBranch是否等于该分支
        if (ProjectManager.rootBranch != targetBranch) gits.add(0, ProjectManager.projectRoot)

        gits.forEach {
            if (!GitUtils.checkIfClean(ProjectManager.findGit(it.absolutePath))) {
                Tools.printError("${it.name} -> checkIfClean :false, please check your file!")
            }
        }
        gits.forEach {
            val check = GitUtils.checkoutBranch(ProjectManager.findGit(it.absolutePath), targetBranch, true)
            if (!check) fail.add(it.name)
        }
        IdeUtils.writeResult("CheckOutTask -> $fail", fail.size)
    }
}