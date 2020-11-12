package com.pqixing.modularization.base

import com.pqixing.EnvKeys
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.modularization.root.getArgs
import com.pqixing.tools.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class XPlugin : Plugin<Project> {
    lateinit var dpsManager: DpsManager
    override fun apply(pro: Project) {
        val args = pro.getArgs()
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
    }

}