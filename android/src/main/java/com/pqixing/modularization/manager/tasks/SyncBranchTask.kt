package com.pqixing.modularization.manager.tasks

import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

/**
 * 同步工程的代码和分支,为了Jekens 构建使用
 */
open class SyncBranchTask : BaseTask() {
    override fun runTask() {
        val extends = project.getArgs()
        var targetBranch = EnvKeys.opBranch.getEnvValue()?:return
        //获取需要更新的模块
        var modules = EnvKeys.tagBranch.getEnvValue()?.split(",")?.map { it.trim() }?.toSet()?: emptySet()
        if (targetBranch.isEmpty()) ResultUtils.writeResult("targetBranch can not be empty", -1);

        val fail = ArrayList<String>()
        GitUtils.open(extends.env.basicDir)?.apply {
            val check =GitUtils.checkoutBranch(this, targetBranch, true)&&GitUtils.pull(this)//更新
            if (!check) fail.add(project.rootDir.name)
            close()
        }
        //更新相关的工程
        extends.manifest.allModules().filter { modules.contains(it.name) }.map { it.project }.toSet().forEach {
            val dir = File(extends.env.codeRootDir, it.path)
            if (GitUtils.isGitDir(dir)) GitUtils.open(dir)?.apply {
                val check = GitUtils.checkoutBranch(this, targetBranch, true)&&GitUtils.pull(this)//更新
                if (!check) fail.add(dir.name)
                close()
            }else GitUtils.clone(it.url,dir,targetBranch)
        }

        ResultUtils.writeResult("SyncBranch -> $fail", fail.size)
    }

    init {
        group = Keys.GROUP_OTHER
    }
}