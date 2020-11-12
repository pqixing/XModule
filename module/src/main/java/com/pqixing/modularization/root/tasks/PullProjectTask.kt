package com.pqixing.modularization.root.tasks

import com.pqixing.help.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.root.getArgs
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
        GitUtils.open(project.getArgs().env.basicDir)?.apply {
            GitUtils.pull(this)
            close()
        }
        project.getArgs().manifest.projects.forEach {
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