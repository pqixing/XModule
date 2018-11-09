package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

class ManagerPlugin : BasePlugin() {
    override val ignoreFields: Set<String>
        get() = setOf(FileNames.PROJECT_INFO, FileNames.IMPORT_KT, FileNames.DOCUMENT)

    @Override
    override fun linkTask() = listOf(AllCleanTask::class.java)

    var error: String = ""
    override fun apply(project: Project) {
        super.apply(project)
        val extends = project.extensions.create("manager", ManagerExtends::class.java, project)

        error = FileManager.checkFileExist(this)

        //在每个工程开始同步之前，检查状态，下载，切换分支等等
        project.gradle.beforeProject {
            ProjectManager.checkProject(it, this, projectInfo!!)
        }

        project.afterEvaluate {
            FileManager.checkDocument(this)
            if (error.isNotEmpty()) {
                ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, error)
            }
            extends.checkVail()
        }


    }
}
