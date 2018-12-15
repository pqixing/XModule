package com.pqixing.modularization.android.tasks

import com.pqixing.Tools
import com.pqixing.git.Components
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.utils.IdeUtils
import com.pqixing.tools.TextUtils
import java.io.File

open class BuildApk : BaseTask() {
    var outputFile: File? = null

    //解析出第一个Dev渠道的构建任务，防止有渠道包
    init {
        project.afterEvaluate {
            val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
            val plugin = AndroidPlugin.getPluginByProject(project)
            val rxForRun = Regex(".*?Dev")
            val androidOut = extHelper.getAndroidOut(project, if (plugin.BUILD_TYPE == Components.TYPE_APPLICATION) "application" else "library")
            Tools.println("---------> $androidOut")
            for (t in androidOut) {
                if (t.key.matches(rxForRun)||t.key == "dev") {
                    this@BuildApk.dependsOn("assemble${TextUtils.firstUp(t.key)}")
                    outputFile = t.value
                    return@afterEvaluate
                }
            }
        }
    }

    override fun runTask() {
        if (outputFile == null || !outputFile!!.exists() || !outputFile!!.name.endsWith(".apk")) {
            Tools.printError("Can not fount apk with path :${outputFile?.absolutePath}")
        } else {
            IdeUtils.writeResult(outputFile!!.absolutePath)
        }

    }
}