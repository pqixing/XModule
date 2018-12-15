package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.tasks.*
import com.pqixing.modularization.maven.IndexVersionTask
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

open class ManagerPlugin : BasePlugin() {
    override fun callBeforeApplyMould() {
        project.extensions.create("manager", ManagerExtends::class.java, project)
    }

    override val applyFiles: List<String> = listOf("com.module.manager", "com.module.git")
    override val ignoreFields: Set<String>
        get() = setOf(FileNames.PROJECT_INFO, FileNames.IMPORT_KT)

    @Override
    override fun linkTask() = listOf(CloneProjectTask::class.java
            , CreateBranchTask::class.java
            , CheckOutTask::class.java
            ,PullProjectTask::class.java
            , MergeTask::class.java, CleanProjectTask::class.java, IndexVersionTask::class.java)

    var error: String = ""
    override fun apply(project: Project) {
        plugin = this
        val startTime = System.currentTimeMillis()
        super.apply(project)
        error = FileManager.checkFileExist(this)


        project.gradle.beforeProject {
            //在每个工程开始同步之前，检查状态，下载，切换分支等等
            ProjectManager.checkProject(it, projectInfo)
        }
        project.afterEvaluate {
            val extends = getExtends(ManagerExtends::class.java)
            extHelper.setExtValue(project, "groupName", extends.groupName)
            FileManager.checkDocument(this)

            if (error.isNotEmpty()) {
                ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, error)
            }
            extends.checkVail()

            project.allprojects { p ->
                extHelper.addRepositories(p, extends.dependMaven)
            }
        }

        project.gradle.addBuildListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                BasePlugin.onClear()
                Tools.println("buildFinished -> spend: ${System.currentTimeMillis() - startTime} ms")
            }
        })
    }

    companion object {
        private lateinit var plugin: ManagerPlugin

        fun getManagerPlugin() = plugin
        fun getManagerExtends() = plugin.getExtends(ManagerExtends::class.java)
    }
}
