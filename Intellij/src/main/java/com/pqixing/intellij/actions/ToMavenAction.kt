package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.ToMavenDialog
import com.pqixing.intellij.utils.GradleUtils
import java.io.File

class ToMavenAction : AnAction() {
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return

        val module = e.getData(DataKey.create<Module>("module"))
        val moduleName = module?.name ?: ""

        val projectMode = /*"ProjectViewPopup".equals(place)||*/"MainMenu" == e.place || module == null || project.name == moduleName;
        var filters = if (projectMode) null else ModuleRootManager.getInstance(module!!).dependencies.map { it.name }

        //需要ToMaven的模块
        val tModules = ModuleManager.getInstance(project).sortedModules
                .filter { filters == null || it.name == moduleName || filters.contains(it.name) }
                .map { JListInfo(it.name, "", 0, it.name == moduleName || it.name == "${moduleName}_api") }


        val dialog = ToMavenDialog(tModules);
        dialog.setOnOk { toMaven(dialog, tModules) }
        dialog.pack()
        dialog.isVisible = true
    }

    private fun toMaven(dialog: ToMavenDialog, tModules: List<JListInfo>) = object : Runnable {
        var i = -1
        var check = false//是否需要校验结果
        var runTaskId = ""
        val logFile = GradleUtils.getLogFile(project.basePath!!)
        override fun run() {
            if (check) {//检查上传的任务是否正确
                val result = GradleUtils.getResult(logFile, runTaskId)
                var succes = result.first
                val jListInfo = tModules[i]
                jListInfo.staue = if (succes) 1 else 3
                jListInfo.log = result.second
                dialog.updateUI(!succes)
                if (!succes) return
            }
            val info = tModules[++i]
            if (info.select && info.staue != 2) {
                check = true
                runTaskId = System.currentTimeMillis().toString()
                GradleUtils.runTask(project, listOf(":${info.title}:clean", ":${info.title}:ToMaven"), runTaskId = runTaskId, callback = this)
            } else {
                check = false
                runTaskId = ""
            }
        }
    }.run()
}