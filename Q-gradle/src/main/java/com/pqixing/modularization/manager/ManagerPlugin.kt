package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.interfaces.ILog
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.tasks.*
import com.pqixing.modularization.maven.VersionIndexTask
import com.pqixing.modularization.maven.VersionTagTask
import com.pqixing.modularization.utils.Logger
import com.pqixing.modularization.utils.ResultUtils
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

open class ManagerPlugin : BasePlugin() {
    init {
        Tools.logger = Logger()
    }
    override fun callBeforeApplyMould() {

    }

    override val applyFiles: List<String> = listOf()
    override val ignoreFields: Set<String>
        get() = setOf(FileNames.USER_CONFIG, FileNames.IMPORT_KT)

    lateinit var args: ArgsExtends

    @Override
    override fun linkTask() = listOf(
            CloneProjectTask::class.java
            , CleanProjectTask::class.java
            , CheckOutTask::class.java
            , LoadAllBranchModuleTask::class.java
            , CreateBranchTask::class.java
            , PullProjectTask::class.java
            , VersionTagTask::class.java
            , VersionIndexTask::class.java
            , DeleteBranchTask::class.java
            , SyncBranchTask::class.java
    )


    override fun apply(project: Project) {
        val startTime = System.currentTimeMillis()
        //在每个工程开始同步之前，检查状态，下载，切换分支等等
        project.gradle.beforeProject { ProjectManager.checkProject(it) }

        args = ArgsExtends().load(project)

        super.apply(project)

        FileManager.checkFileExist(project)

        FileManager.trySyncFile(args)

        project.afterEvaluate {
            val extends = getExtends(ArgsExtends::class.java)
            extHelper.setExtValue(project, "groupName", extends.projectXml.mavenGroup)
            FileManager.checkDocument(this)
            project.allprojects { p ->
                extHelper.addRepositories(p, arrayListOf(extends.projectXml.mavenUrl))
            }
        }

        project.gradle.addListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                ResultUtils.notifyBuildFinish(project, System.currentTimeMillis() - startTime)
            }
        })
    }
}

fun Project.getEnv() = project.rootProject.plugins.getPlugin(ManagerPlugin::class.java)
fun Project.getArgs() = getEnv().args
