package com.pqixing.modularization.manager

import com.pqixing.modularization.FileNames
import com.pqixing.modularization.setting.SettingPlugin
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.tasks.*
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
    )

    override fun apply(project: Project) {
        super.apply(project)
        val args = SettingPlugin.findArgs(project).projectXml

        project.allprojects { p -> extHelper.addRepositories(p, arrayListOf(args.mavenUrl)) }
    }
}

fun Project.rootPlugin() = SettingPlugin.findPlugin(this.rootProject) as RootPlugin
fun Project.getArgs() = SettingPlugin.findArgs(this)
