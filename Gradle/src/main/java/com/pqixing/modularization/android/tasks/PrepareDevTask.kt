package com.pqixing.modularization.android.tasks

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
        val devDir = File(project.projectDir, "src/dev")
        val from = File(FileManager.docRoot, "android")

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
        ResultUtils.writeResult("PrepareDevTask -> finish")
    }
}