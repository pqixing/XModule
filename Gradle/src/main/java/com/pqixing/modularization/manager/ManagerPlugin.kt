package com.pqixing.modularization.manager

import com.pqixing.git.GitUtils
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.forOut.ProjectInfo
import org.gradle.api.Project
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

class ManagerPlugin : BasePlugin() {
    override fun getIgnoreFields() = setOf(FileNames.PROJECT_INFO, FileNames.INCLUDE_KT, FileNames.DOCUMENT)

    @Override
    override fun linkTask() = listOf(AllCleanTask::class.java)

    var error: String = ""
    override fun apply(project: Project) {
        super.apply(project)
        project.extensions.create("Manager", ManagerExtends::class.java, project)
        error = FileManager.checkFileExist(this)

        //在每个工程开始同步之前，检查状态，下载，切换分支等等
        project.gradle.beforeProject {
            checkProject(it, projectInfo)
        }
        project.afterEvaluate {
            FileManager.checkDocument(this)
            if (error.isNotEmpty()) {
                ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, error)
            }
        }
    }


    /**
     * 检查每个子工程的状态，分支信息等
     */
    private fun checkProject(project: Project, info: ProjectInfo) {
        val buildDir = info.buildDir.toString().trim()
        if (buildDir.isNotEmpty()) project.buildDir = File(project.buildDir, buildDir)

        val projectDir = GitUtils.findGitDir(project.projectDir)

        if (projectDir?.exists() != true) {//下载工程

        } else if (!GitUtils.isGitDir(projectDir)) {//该目录已经存在，但是不是git工程

        } else if (info.syncBranch) {
            checkBranch()
        }


    }

    private fun checkBranch() {

    }


}
