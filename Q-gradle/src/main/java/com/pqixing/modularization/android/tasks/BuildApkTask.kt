package com.pqixing.modularization.android.tasks

import com.android.build.gradle.AppExtension
import com.pqixing.Tools
import com.pqixing.EnvKeys
import com.pqixing.getEnvValue
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class BuildApkTask : BaseTask() {
    private var buildType: String
    private var buildApkPath: String?

    //解析出第一个Dev渠道的构建任务，防止有渠道包
    init {
        buildType = EnvKeys.buildApkType.getEnvValue() ?: "release"
        buildApkPath = EnvKeys.buildApkPath.getEnvValue()
        this.dependsOn("assemble${TextUtils.firstUp(buildType)}")
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
        }else if(!copyFile.exists()){
            copyFile.mkdirs()
        }

        if (copyFile.isDirectory) {
            FileUtils.copy(newOutFile, File(copyFile, newOutFile.name))
            ResultUtils.writeResult("${outPath}/${newOutFile.name}")
        }
    }
}