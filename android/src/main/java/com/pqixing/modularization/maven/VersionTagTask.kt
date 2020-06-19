package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.getArgs

/**
 * 从往上初始化版本号
 * 创建分支依赖版本号
 */
open class VersionTagTask : BaseTask() {
    init {
        //先更新所有版本号
        this.dependsOn("VersionIndex")
    }

    override fun runTask() {
        project.getArgs().versions.createVersionTag()
    }


}
