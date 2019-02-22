package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

open class CreateBranchTask : BaseTask() {
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

        val fail = ArrayList<String>()
        val rootDir = ManagerPlugin.getPlugin().rootDir
        ProjectManager.projectXml.projects
                .map { File(ProjectManager.codeRootDir, it.name) }
                .toMutableList().apply {
                    add(rootDir)
                }.forEach {
                    val git = GitUtils.open(it) ?: return@forEach
                    if (!GitUtils.checkIfClean(git) || !GitUtils.createBranch(git, targetBranch)) fail.add(it.name)
                }
        ResultUtils.writeResult("CreateBranchTask -> $fail", fail.size)
    }
}