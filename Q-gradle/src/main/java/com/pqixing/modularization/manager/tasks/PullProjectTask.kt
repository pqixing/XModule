package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import java.io.File

/**
 * 切换分支
 */
open class PullProjectTask : BaseTask() {
    init {
        group = Keys.GROUP_OTHER
    }

    override fun runTask() {
        GitUtils.open(project.getArgs().env.templetRoot)?.apply {
            GitUtils.pull(this)
            close()
        }
        project.getArgs().projectXml.projects.forEach {
            val dir = File(project.getArgs().env.codeRootDir, it.path)
            if (!GitUtils.isGitDir(dir)) return@forEach
            Tools.println("          start pull-> ${dir.name} ${it.url}")
            GitUtils.open(dir)?.apply {
                GitUtils.pull(this)
                close()
            }
        }
    }
}