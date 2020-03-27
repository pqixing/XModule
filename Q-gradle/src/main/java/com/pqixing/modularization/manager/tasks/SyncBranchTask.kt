package com.pqixing.modularization.manager.tasks

import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

/**
 * 同步工程的代码和分支,为了Jekens 构建使用
 */
open class SyncBranchTask : BaseTask() {
    override fun runTask() {
        val extends = ManagerPlugin.getExtends()
        var targetBranch = EnvKeys.opBranch.getEnvValue()?:return
        //获取需要更新的模块
        var modules = EnvKeys.tagBranch.getEnvValue()?.split(",")?.map { it.trim() }?.toSet()?: emptySet()
        if (targetBranch.isEmpty()) ResultUtils.writeResult("targetBranch can not be empty", -1);

        val fail = ArrayList<String>()
        GitUtils.open(FileManager.templetRoot)?.apply {
            val check =GitUtils.checkoutBranch(this, targetBranch, true)&&GitUtils.pull(this)//更新
            if (!check) fail.add(project.rootDir.name)
            close()
        }
        //更新相关的工程
        ProjectManager.projectXml.allSubModules().filter { modules.contains(it.name) }.map { it.project }.toSet().forEach {
            val dir = File(ProjectManager.codeRootDir, it.name)
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