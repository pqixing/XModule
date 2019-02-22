package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

open class DeleteBranchTask : BaseTask() {
    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject")
    }

    override fun runTask() {
        val extends = ManagerPlugin.getExtends()
        val info = extends.config
        if (info.screctKey != Keys.SCRECTKEY) {
            Tools.printError("DeleteBranch Exception -> check password error!!!")
        }
        var targetBranch = info.taskBranch
        if (targetBranch == extends.docRepoBranch) Tools.printError("DeleteBranchTask Exception -> Can not delete current branch $targetBranch , please change branch before delete!!")
        if (targetBranch == "master") Tools.printError("DeleteBranchTask Exception -> Can not delete master !!")

        val fail = ArrayList<String>()
        val rootDir = ManagerPlugin.getPlugin().rootDir
        ProjectManager.projectXml.projects
                .map { File(ProjectManager.codeRootDir, it.name) }
                .toMutableList().apply {
                    add(rootDir)
                }.forEach {
                    val git = GitUtils.open(it) ?: return@forEach
                    if (!GitUtils.pull(git) || !GitUtils.delete(git, targetBranch)) fail.add(it.name)
                }
        ResultUtils.writeResult("DeleteBranchTask -> $fail", fail.size)
    }
}
