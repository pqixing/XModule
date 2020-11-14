package com.pqixing.modularization.base

import com.pqixing.EnvKeys
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.maven.IndexMavenTask
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.modularization.root.tasks.*
import com.pqixing.modularization.setting.ImportPlugin
import com.pqixing.tools.FileUtils
import org.gradle.api.Project
import java.io.File

/**
 * Created by pqixing on 17-12-20.
 * 管理代码工程的导入，maven仓库的依赖的版本生成
 */

open class XPlugin : BasePlugin() {
    lateinit var dpsManager: DpsManager
    override val ignoreFields: Set<String>
        get() = setOf(EnvKeys.USER_CONFIG)


    @Override
    override fun linkTask() = listOf(
            CloneProjectTask::class.java
            , CleanProjectTask::class.java
            , CheckOutTask::class.java
            , LoadAllBranchModuleTask::class.java
            , CreateBranchTask::class.java
            , PullProjectTask::class.java
            , DeleteBranchTask::class.java
            , SyncBranchTask::class.java
            , IndexMavenTask::class.java
    )

    override fun apply(pro: Project) {
        super.apply(project)
        val args = ImportPlugin.findArgs(project)

        val module = args.manifest.findModule(pro.name) ?: return

        dpsManager = DpsManager(pro, module)
        if (module.forMaven) {
            BaseTask.task(pro, ToMavenTask::class.java)
        }
        pro.afterEvaluate {
            val cacheDir = File(pro.projectDir, "build/${pro.buildDir.name}")
            //解析依赖
            pro.apply(mapOf("from" to FileUtils.writeText(File(cacheDir, EnvKeys.GRADLE_DEPENDENCIES), dpsManager.resolveDps(), true)))
        }

        project.allprojects { p -> extHelper.addRepositories(p, arrayListOf(args.manifest.mavenUrl)) }
    }

}

