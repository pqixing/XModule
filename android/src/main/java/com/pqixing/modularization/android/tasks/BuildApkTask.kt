package com.pqixing.modularization.android.tasks

import com.android.build.gradle.AppExtension
import com.pqixing.Tools
import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.android.pluginModule
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.root.getArgs
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class BuildApkTask : BaseTask() {
    val plugin = project.pluginModule()
    private lateinit var buildType: String
    private var buildApkPath: String? = null

    //解析出第一个Dev渠道的构建任务，防止有渠道包

    override fun prepare() {
        super.prepare()
        buildType = if (!plugin.isApp) "dev" else EnvKeys.buildApkType.getEnvValue() ?: "release"
        this.dependsOn("assemble${TextUtils.firstUp(buildType)}")
    }

    override fun whenReady() {
        super.whenReady()
        buildApkPath = EnvKeys.buildApkPath.getEnvValue()
        if (!plugin.isApp && !plugin.module.attach()) createSrc()
    }

    fun createSrc() {
        val devDir = File(project.projectDir, "src/dev")
        val from = File(project.getArgs().env.basicDir, "android")
        val manifest = "AndroidManifest.xml"
        File(devDir, manifest).apply {
            if (!exists()) FileUtils.writeText(this, FileUtils.readText(File(from, manifest))
                    ?.replace("[projectName]", TextUtils.numOrLetter(project.name)) ?: "", true)
        }

        val devJavaDir = File(project.projectDir, "src/dev/java")
        arrayOf("DevApplication.java", "DevActivity.java").forEach {
            File(devJavaDir, it).apply {
                if (!exists()) FileUtils.writeText(this, FileUtils.readText(File(from, it))
                        ?.replace("[projectName]", TextUtils.numOrLetter(project.name)) ?: "", true)
            }
        }
    }

    override fun runTask() {
        Tools.println("BuildApk Type -> $buildType")
        val application: AppExtension = project.extensions.getByName("android") as? AppExtension
                ?: return
        val find = application.applicationVariants.find { it.buildType.name == buildType } ?: return

        val outFile = find.outputs.last().outputFile

        val newOutFile = File(outFile.parentFile, "${project.name}-${find.versionCode}-${find.versionName}-${SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())}.apk")

        outFile.renameTo(newOutFile)

        val outPath = buildApkPath ?: return ResultUtils.writeResult(newOutFile.absolutePath)

        val copyFile = File(outPath)
        if (copyFile.name.endsWith(".apk")) {
            FileUtils.copy(newOutFile, copyFile)
            ResultUtils.writeResult(outPath)
        } else if (!copyFile.exists()) {
            copyFile.mkdirs()
        }

        if (copyFile.isDirectory) {
            FileUtils.copy(newOutFile, File(copyFile, newOutFile.name))
            ResultUtils.writeResult("${outPath}/${newOutFile.name}")
        }
    }
}