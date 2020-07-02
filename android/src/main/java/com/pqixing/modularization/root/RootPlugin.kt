package com.pqixing.modularization.root

import com.pqixing.modularization.FileNames
import com.pqixing.modularization.setting.ImportPlugin
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.root.tasks.*
import com.pqixing.modularization.maven.VersionIndexTask
import com.pqixing.modularization.maven.VersionTagTask
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

open class RootPlugin : BasePlugin() {
    override val ignoreFields: Set<String>
        get() = setOf(FileNames.USER_CONFIG)


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
            , IndexVersionTask::class.java
    )

    override fun apply(project: Project) {
        super.apply(project)
        val args = ImportPlugin.findArgs(project).manifest
//        val reIndexTask  =  project.task("ReIndex")
//        project.afterEvaluate {
//
//        }
        project.allprojects { p -> extHelper.addRepositories(p, arrayListOf(args.mavenUrl)) }
    }
}

fun Project.rootPlugin() = ImportPlugin.findPlugin(this.rootProject) as RootPlugin
fun Project.getArgs() = ImportPlugin.findArgs(this)
