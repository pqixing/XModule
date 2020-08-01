package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.group.XModuleGroup
import com.pqixing.intellij.ui.ToMavenDialog
import com.pqixing.intellij.utils.TaskCallBack
import com.pqixing.intellij.utils.GradleUtils

class ToMavenAction : AnAction() {
    lateinit var project: Project
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = XModuleGroup.hasBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val module = e.getData(DataKey.create<Module>("module"))
        val allModule = XmlHelper.loadManifest(project.basePath)?.allModules()?: mutableSetOf()
        val target = allModule.find { it.name == module?.name }?.takeIf { it.isAndroid }

        val moduleName = target?.name ?: ""

        val projectMode = "MainMenu" == e.place || target == null

        val filters = (if (projectMode) allModule.filter { it.isAndroid }.map { it.name } else ModuleRootManager.getInstance(module!!).dependencies.map { it.name }).toMutableSet()//过滤Android工程
        filters.remove(project.name)
        filters.add(moduleName)

        //需要ToMaven的模块
        val tModules = ModuleManager.getInstance(project).sortedModules
                .filter { filters.contains(it.name) }
                .map { JListInfo(it.name, "", 0, it.name == moduleName || it.name == "${moduleName}_api") }

        val dialog = ToMavenDialog(project, tModules, moduleName);
        dialog.setOnOk { toMaven(dialog, tModules) }
        dialog.pack()
        dialog.isVisible = true
    }

    private fun toMaven(dialog: ToMavenDialog, tModules: List<JListInfo>) = object : TaskCallBack {
        var i = -1
        var check = false//是否需要校验结果
        var runTaskId = ""
        var excute = 0
        var envs = GradleUtils.defEnvs.toMutableMap().apply { put(EnvKeys.toMavenUnCheck, dialog.unCheckCode) }

        override fun onTaskEnd(success: Boolean, result: String?) {
            if (check) {//检查上传的任务是否正确
                val jListInfo = tModules[i]
                jListInfo.staue = if (success) 1 else 3
                jListInfo.log = result ?: ""
                //失败是展示结果
                dialog.updateUI(true)

                if (!success) return
            }
            if (i >= tModules.size - 1) {
                dialog.updateUI(false)
                return
            }//执行完毕
            val info = tModules[++i]
            if (info.select && info.staue != 1) {
                check = true
                excute++
                runTaskId = System.currentTimeMillis().toString()
                info.staue = 2//正在执行
                GradleUtils.runTask(project, listOf(":${info.title}:ToMaven"), activateToolWindowBeforeRun = excute == 1, envs = envs, runTaskId = runTaskId, callback = this)
            } else {
                check = false
                runTaskId = ""
                this.onTaskEnd(false, null)//循环运行
            }
        }
    }.onTaskEnd(false, null)
}