package com.pqixing.modularization.root.tasks

import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.root.getArgs
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
        args.manifest.projects
                .map { File(args.env.codeRootDir, it.path) }
                .toMutableList().apply {
                    add(args.env.basicDir)
                }.forEach {
                    val git = GitUtils.open(it) ?: return@forEach
                    if (!GitUtils.checkIfClean(git) || !GitUtils.createBranch(git, targetBranch)) fail.add(it.name)
                }
        ResultUtils.writeResult("CreateBranchTask -> $fail", fail.size)
    }
}