package com.pqixing.intellij.gradle

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.EnvKeys
import com.pqixing.intellij.XApp
import com.pqixing.intellij.git.uitils.GitHelper
import com.pqixing.tools.FileUtils
import java.io.File

object GradleUtils {

    fun runTask(project: Project, tasks: List<String>, runTaskId: String = "", envs: Map<String, String> = emptyMap(), callback: TaskCallBack? = null) {

        GradleRequest(tasks, envs).runGradle(project) { callback?.onTaskEnd(it.success, it.getDefaultOrNull()) }
        XApp.log("runTask end -> $project")
    }

    fun downloadBasic(target: Project, dir: File?, base: String?, after: () -> Unit) = XApp.invoke {
        val url = base
                ?: Messages.showInputDialog("Input your basic git url to init project", "Download Basic", null, "https://github.com/pqixing/md_demo.git", null)?.takeIf { it.isNotEmpty() }
                ?: return@invoke
        val basicDir = dir ?: File(target.basePath, EnvKeys.BASIC)
        if (basicDir.exists()) {
            val exitCode = Messages.showOkCancelDialog(target, "basic dir is not empty!!!", "DELETE", "DEL", "CANCEL", null)
            if (exitCode != Messages.OK) return@invoke
            FileUtils.delete(basicDir)
        }
        XApp.runAsyn(target, "Download Basic") { indicator ->
            indicator.text = "clone $url"
            kotlin.runCatching { GitHelper.clone(target, basicDir, url) }
            after()
        }
    }
}
