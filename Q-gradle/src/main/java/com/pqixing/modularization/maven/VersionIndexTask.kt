package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.ResultUtils
import java.io.File

/**
 * 从往上初始化版本号
 */
open class VersionIndexTask : BaseTask() {
    override fun start() {

    }

    override fun runTask() {
        project.getArgs().versions.indexVersionFromNet()
    }

    override fun end() {
        //执行完成，输出文件路径
        ResultUtils.writeResult(File(project.getArgs().env.templetRoot, "versions/version.properties").absolutePath)
    }
}
