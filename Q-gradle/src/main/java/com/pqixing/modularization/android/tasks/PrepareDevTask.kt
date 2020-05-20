package com.pqixing.modularization.android.tasks

import com.pqixing.model.SubModuleType
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.android.MDPlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.FileManager
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import java.io.File

open class PrepareDevTask : BaseTask() {
    override fun start() {
    }

    override fun runTask() {
        if (project.MDPlugin().subModule.type == SubModuleType.TYPE_APPLICATION) {
            return//如果是App类型,不需要设置运行
        }
        val devDir = File(project.projectDir, "src/dev")
        val from = File(FileManager.templetRoot, "android")

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

    override fun end() {
//        ResultUtils.writeResult("PrepareDevTask -> finish")
    }
}