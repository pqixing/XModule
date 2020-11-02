package com.pqixing.intellij.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.EnvKeys
import com.pqixing.intellij.gradle.GradleRequest
import com.pqixing.tools.FileUtils
import java.io.File

object GradleUtils {

    fun runTask(project: Project, tasks: List<String>, runTaskId: String = "", envs: Map<String, String> = emptyMap(), callback: TaskCallBack? = null) {

        GradleRequest(tasks, envs).runGradle(project) { callback?.onTaskEnd(it.success, it.getDefaultOrNull()) }
    }

    fun downloadBasic(target: Project, dir: File?, base: String?, after: () -> Unit) = ApplicationManager.getApplication().invokeLater {
        val url = base
                ?: Messages.showInputDialog("Input your basic git url to init project", "Download Basic", null, "https://github.com/pqixing/md_demo.git", null)?.takeIf { it.isNotEmpty() }
                ?: return@invokeLater
        val basicDir = dir ?: File(target.basePath, EnvKeys.BASIC)
        if (basicDir.exists()) {
            val exitCode = Messages.showOkCancelDialog(target, "basic dir is not empty!!!", "DELETE", "DEL", "CANCEL", null)
            if (exitCode != Messages.OK) return@invokeLater
            FileUtils.delete(basicDir)
        }
        val importTask = object : Task.Backgroundable(target, "Download Basic") {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "clone $url"
                kotlin.runCatching { GitHelper.clone(target, basicDir, url) }
                after()
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(importTask, BackgroundableProcessIndicator(importTask))
    }
}
