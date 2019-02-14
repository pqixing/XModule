package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.ResultUtils

open class DeleteBranchTask : BaseTask() {
    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject")
    }

    override fun runTask() {
        val info = ManagerPlugin.getManagerPlugin().projectInfo
        if (info.password != "delete") {
            Tools.printError("DeleteBranch Exception -> check password error!!!")
        }
        var targetBranch = info.taskBranch
        if (targetBranch == ProjectManager.rootBranch) Tools.printError("DeleteBranchTask Exception -> Can not delete current branch $targetBranch , please change branch before delete!!")
        if (targetBranch == "master") Tools.printError("DeleteBranchTask Exception -> Can not delete master !!")

        val gits = ProjectManager.findAllGitPath().values.filter { it.exists() }.toMutableList()
        gits.add(0, ProjectManager.projectRoot)

        val fail = ArrayList<String>()
        gits.forEach {
            if (!it.exists()) return@forEach
            val create = GitUtils.delete(ProjectManager.findGit(it.absolutePath), targetBranch)
            if (!create) fail.add(it.name)
        }
        ResultUtils.writeResult("DeleteBranchTask -> $fail", fail.size)
    }
}
