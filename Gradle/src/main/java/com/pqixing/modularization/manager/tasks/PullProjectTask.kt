package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ProjectManager
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
        GitUtils.open(project.rootDir)?.apply {
            GitUtils.pull(this)
            close()
        }
        ProjectManager.projectXml.projects.forEach {
            val dir = File(ProjectManager.codeRootDir, it.name)
            if (!GitUtils.isGitDir(dir)) return@forEach
            Tools.println("          start pull-> ${dir.name} ${it.url}")
            GitUtils.open(dir)?.apply {
                GitUtils.pull(this)
                close()
            }
        }
    }
}