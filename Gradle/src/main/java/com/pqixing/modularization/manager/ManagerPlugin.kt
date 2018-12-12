package com.pqixing.modularization.manager

import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.maven.IndexVersionTask
import org.gradle.BuildAdapter
import org.gradle.BuildListener
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

class ManagerPlugin : BasePlugin() {
    override fun callBeforeApplyMould() {
        project.extensions.create("manager", ManagerExtends::class.java, project)
    }

    override val applyFiles: List<String> = listOf("com.module.manager", "com.module.git")
    override val ignoreFields: Set<String>
        get() = setOf(FileNames.PROJECT_INFO, FileNames.IMPORT_KT, FileNames.MANAGER)

    @Override
    override fun linkTask() = listOf(AllCleanTask::class.java, IndexVersionTask::class.java)

    var error: String = ""
    override fun apply(project: Project) {
        onSyncStart()
        super.apply(project)
        error = FileManager.checkFileExist(this)


        project.gradle.beforeProject {
            //在每个工程开始同步之前，检查状态，下载，切换分支等等
            ProjectManager.checkProject(it, projectInfo!!)
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
            override fun projectsEvaluated(gradle: Gradle) {
                ProjectManager.gitForProject.forEach { it.value.close() }
                ProjectManager.gitForProject.clear()

            }
        })
    }

    fun onSyncStart() {
        //构建结束时，重置projectInfo
        pi = null
        FileManager.cacheRoot = null
        FileManager.codeRootDir = null
        ProjectManager.allComponents.clear()
        ProjectManager.gitForProject.clear()
        ProjectManager.hasInit = false
        ProjectManager.rootBranch = ""
    }
}
