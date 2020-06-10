package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.interfaces.ILog
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.tasks.*
import com.pqixing.modularization.maven.VersionIndexTask
import com.pqixing.modularization.maven.VersionTagTask
import com.pqixing.modularization.utils.Logger
import com.pqixing.modularization.utils.ResultUtils
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
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
        allPlugins[project] = this
        val startTime = System.currentTimeMillis()
        //在每个工程开始同步之前，检查状态，下载，切换分支等等
//        project.gradle.beforeProject { ProjectManager.checkProject(it) }

        args = ArgsExtends().load(project)

        super.apply(project)

        ProjectManager.checkFileExist(project)

        ProjectManager.trySyncFile(args)

//        project.afterEvaluate {
        extHelper.setExtValue(project, "groupName", args.projectXml.mavenGroup)
        ProjectManager.checkDocument(this)


        project.allprojects { p ->
            ProjectManager.checkProject(p)
            extHelper.addRepositories(p, arrayListOf(args.projectXml.mavenUrl))
        }
//        }

        project.gradle.addListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                args.clear()
                allPlugins.remove(project)
                project.allprojects { allPlugins.remove(it) }

                ResultUtils.notifyBuildFinish(project, System.currentTimeMillis() - startTime)
            }
        })
    }
}

internal val allPlugins = hashMapOf<Project, Plugin<Project>>()

fun Project.getEnv() = allPlugins[project.rootProject] as ManagerPlugin
fun Project.getArgs() = getEnv().args
