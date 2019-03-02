package com.pqixing.modularization.android.tasks

import com.pqixing.Tools
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.io.File

open class BuildApkTask : BaseTask() {
    var outputFile: File? = null

    //解析出第一个Dev渠道的构建任务，防止有渠道包
    init {
        project.afterEvaluate { p ->
            val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
            val plugin = AndroidPlugin.getPluginByProject(project)

            val types = arrayListOf("dev", "debug","inTest", "outTest", "release")
            val apkType = TextUtils.getSystemEnv("buildApkType")
            if (apkType?.isNotEmpty() == true) {
                types.remove(apkType)
                types.add(0, apkType)
            }
            val androidOut = extHelper.getAndroidOut(project, if (plugin.buildAsApp) "application" else "library")

            for (t in types) {
                outputFile = androidOut[t] ?: continue
                this@BuildApkTask.dependsOn("assemble${TextUtils.firstUp(t)}")
                break
            }

        }
    }

    override fun runTask() {

        if (outputFile == null || !outputFile!!.exists() || !outputFile!!.name.endsWith(".apk")) {
            Tools.printError(-1,"Can not fount apk with path :${outputFile?.absolutePath}")
        } else {
            var buildApkPath = AndroidPlugin.getPluginByProject(project).config.taskResultFile
            if (buildApkPath.isEmpty()) buildApkPath = outputFile!!.absolutePath
            else FileUtils.copy(outputFile!!, File(buildApkPath))

            ResultUtils.writeResult(buildApkPath)
        }

    }
}