package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.ResultUtils

open class CreateBranchTask : BaseTask() {
    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject", "PullProject")
        project.getTasksByName("PullProject", false).forEach { it.mustRunAfter("CloneProject") }
    }

    override fun runTask() {
        val info = ManagerPlugin.getManagerPlugin().projectInfo

        var targetBranch = info.taskBranch
        if (targetBranch.isEmpty()) targetBranch = ProjectManager.rootBranch

        val gits = ProjectManager.findAllGitPath().values.filter { it.exists() }.toMutableList()
        gits.add(0, ProjectManager.projectRoot)
        gits.forEach {
            if (!GitUtils.checkIfClean(ProjectManager.findGit(it.absolutePath))) {
                Tools.printError("CreateBranchTask -> ${it.name}  checkIfClean :false, please check your file!")
            }
        }

        val fail = ArrayList<String>()
        gits.forEach {
            if (!it.exists()) return@forEach
            val create = GitUtils.createBranch(ProjectManager.findGit(it.absolutePath), targetBranch)
            if (!create) fail.add(it.name)
        }
        //创建分支成功时，同时打上版本标签
        if (fail.isEmpty()) VersionManager.createVersionTag()
        ResultUtils.writeResult("CreateBranchTask -> $fail", fail.size)
    }
}