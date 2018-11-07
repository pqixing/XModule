package com.pqixing.modularization.manager

import com.pqixing.git.GitUtils
import com.pqixing.modularization.ProjectInfo
import org.gradle.api.Project
import java.io.File

object ProjectManager {
    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project, plugin: ManagerPlugin, info: ProjectInfo) {
        val buildDir = info.buildDir.toString().trim()
        if (buildDir.isNotEmpty()) project.buildDir = File(project.buildDir, buildDir)

        val projectDir = GitUtils.findGitDir(project.projectDir)

        if (projectDir?.exists() != true) {//下载工程

        } else if (!GitUtils.isGitDir(projectDir)) {//该目录已经存在，但是不是git工程

        } else if (info.syncBranch) {
        }

    }
}
