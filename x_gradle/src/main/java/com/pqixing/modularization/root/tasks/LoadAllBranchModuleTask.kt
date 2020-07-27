package com.pqixing.modularization.root.tasks

import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.getEnvValue
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.root.getArgs
import com.pqixing.modularization.utils.ResultUtils

/**
 * 检查需要合并的工程，列出名称和路径
 * 1,下载全部工程代码
 * 2,更新全部代码
 * 3,更新版本信息，查询该分支的仓库中所有上传的模块，列出，合并后需要重新编译这些模块
 * 4,检查所有工程代码是否是Clean,是否和Root分支一致
 * 5，合并所有工程，列出所有冲突工程目录
 * 6，结合1，给出所有需要导入的模块列表（包含要重新编译和有冲突的工程模块），标记需要处理冲突的分支
 */
open class LoadAllBranchModuleTask: BaseTask() {

    init {
        group = Keys.GROUP_OTHER
    }


    override fun runTask() {
        //当前分支
        //需要合并到当前的指定分支
        val checkBranch = EnvKeys.opBranch.getEnvValue()?:return

        //查找出,当前分支,所有上传过的模块,在合并前先导入AS
        val byBranch = project.getArgs().versions.findAllModuleByBranch(checkBranch)

        val result = StringBuilder("")

        project.getArgs().manifest.projects.forEach { p ->
            p.modules.forEach { s ->
                if (s.isApplication) {
                    result.append(s.name).append(",")
                }
            }
        }
        result.append("#")
        byBranch.forEach { result.append("$it,") }
        Tools.println("Check $checkBranch , Suggest import module :")
        ResultUtils.writeResult(result.toString())
    }
}