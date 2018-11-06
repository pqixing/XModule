package com.pqixing.modularization.manager

import com.pqixing.git.GitUtils
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.forOut.ProjectInfo
import com.pqixing.modularization.manager.tasks.*
import com.pqixing.modularization.maven.IndexMavenTask
import org.gradle.api.Project
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 */

class ManagerPlugin : BasePlugin() {
    override fun getIgnoreFields() = setOf<String>("config.gradle", "setting.kt", "local.gradle", "manager.properties", "config2.gradle")


    @Override
    override fun linkTask() = listOf(UpdateCodeTask::class.java, UpdateCodeTask::class.java
            , CheckOutTask::class.java
            , CloneAllTask::class.java
            , VersionTagTask::class.java
            , CreateBranchTask::class.java
            , DeleteBranchTask::class.java
            , IndexMavenTask::class.java
            , LogAllGitTask::class.java
            , FastMergeTask::class.java)

    override fun apply(project: Project) {
        super.apply(project)
        project.extensions.create("Manager", ManagerExtends::class.java, project)
        checkFileExist()

        //在每个工程开始同步之前，检查状态，下载，切换分支等等
        project.gradle.beforeProject {
            checkProject(it, projectInfo)
        }
        project.afterEvaluate {
            checkDocument()
        }
    }

    /**
     * 检查本地Document目录
     * Document 目录用来存放一些公共的配置文件
     */
    private fun checkDocument() {

    }

    private fun checkProject(project: Project, info: ProjectInfo) {
        val buildDir = info.buildDir.toString().trim()
        if (buildDir.isNotEmpty()) project.buildDir = File(project.buildDir, buildDir)
        val projectDir = project.projectDir

        if (!projectDir.exists()) {//下载工程

        } else if (!GitUtils.isGitDir(projectDir)) {//该目录已经存在，但是不是git工程

        } else if (info.syncBranch) {
            checkBranch()
        }


    }

    private fun checkBranch() {

    }

    /**
     * 检测需要导出的文件有没有被导出
     */
    private fun checkFileExist() {

    }
}
