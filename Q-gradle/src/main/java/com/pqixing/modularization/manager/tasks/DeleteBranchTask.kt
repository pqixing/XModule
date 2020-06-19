package com.pqixing.modularization.manager.tasks

import com.pqixing.EnvKeys
import com.pqixing.Tools
import com.pqixing.getEnvValue
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

open class DeleteBranchTask : BaseTask() {
    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject")
    }

    override fun runTask() {
        val extends = project.getArgs()
        if (EnvKeys.screctKey.getEnvValue() != Keys.SCRECTKEY) {
            Tools.printError(-1,"DeleteBranch Exception -> check password error!!!")
        }
        var targetBranch = EnvKeys.opBranch.getEnvValue()?:return
        if (targetBranch == extends.env.basicBranch) Tools.printError(-1,"DeleteBranchTask Exception -> Can not delete current branch $targetBranch , please change branch before delete!!")
        if (targetBranch == "master") Tools.printError(-1,"DeleteBranchTask Exception -> Can not delete master !!")

        val fail = ArrayList<String>()
        extends.projectXml.projects
                .map { File(extends.env.codeRootDir, it.path) }
                .toMutableList().apply {
                    add(extends.env.basicDir)
                }.forEach {
                    val git = GitUtils.open(it) ?: return@forEach
                    if (!GitUtils.pull(git) || !GitUtils.delete(git, targetBranch)) fail.add(it.name)
                }
        ResultUtils.writeResult("DeleteBranchTask -> $fail", fail.size)
    }
}
