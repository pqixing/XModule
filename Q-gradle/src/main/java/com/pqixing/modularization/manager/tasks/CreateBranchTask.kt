package com.pqixing.modularization.manager.tasks

import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

open class CreateBranchTask : BaseTask() {
    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject")
    }

    override fun runTask() {
        var targetBranch = EnvKeys.opBranch.getEnvValue()?:return
        val args = project.getArgs();
        val fail = ArrayList<String>()
        args.projectXml.projects
                .map { File(args.env.codeRootDir, it.name) }
                .toMutableList().apply {
                    add(args.env.templetRoot)
                }.forEach {
                    val git = GitUtils.open(it) ?: return@forEach
                    if (!GitUtils.checkIfClean(git) || !GitUtils.createBranch(git, targetBranch)) fail.add(it.name)
                }
        ResultUtils.writeResult("CreateBranchTask -> $fail", fail.size)
    }
}