package com.pqixing.modularization.manager

import com.pqixing.git.GitUtils
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.forOut.ProjectInfo
import com.pqixing.tools.FileUtils
import org.gradle.api.Project
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

class ManagerPlugin : BasePlugin() {
    override fun getIgnoreFields() = setOf(FileNames.PROJECT_INFO, FileNames.INCLUDE_KT)

    @Override
    override fun linkTask() = listOf(AllCleanTask::class.java)

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
        val manager = getExtends(ManagerExtends::class.java)

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

    /**
     * 检测需要导出的文件有没有被导出
     * 待检测项
     * ${cacheDir}/ImportProject.gradle  若不存在或有更新，替换文件
     * setting.gradle  若不包含指定代码，添加代码
     * include.kt   若不存在，生成模板
     * ProjectInfo.groovy  若不存在，生成模板
     */
    private fun checkFileExist() {
        with(File(FileNames.INCLUDE_KT)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/include.kt"))
        }
        with(File(FileNames.PROJECT_INFO)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/ProjectInfo.groovy"))
        }

        with(File(FileNames.SETTINGS_GRADLE)) {
            if (!exists()) FileUtils.writeText(this, FileUtils.getTextFromResource("setting/settings.gradle"))
            else if (!readText().matches(Regex("//START.*//END"))) {
                appendText(FileUtils.getTextFromResource("setting/settings.gradle"))
            }
            Unit
        }


    }
}
