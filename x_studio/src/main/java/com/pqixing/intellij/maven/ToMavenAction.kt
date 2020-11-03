package com.pqixing.intellij.maven

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XApp
import com.pqixing.intellij.ui.adapter.JListInfo
import com.pqixing.intellij.XGroup
import com.pqixing.intellij.ui.ToMavenDialog
import com.pqixing.intellij.gradle.GradleUtils
import com.pqixing.intellij.gradle.TaskCallBack
import com.pqixing.intellij.utils.UiUtils.realName

class ToMavenAction : AnAction() {
    lateinit var project: Project
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = XGroup.isBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        val module = e.getData(DataKey.create<Module>("module"))
        val allModule = XmlHelper.loadManifest(project.basePath)?.allModules() ?: mutableSetOf()
        val target = allModule.find { it.name == module?.realName() }?.takeIf { it.isAndroid }

        val moduleName = target?.name ?: ""

        val projectMode = "MainMenu" == e.place || target == null

        val filters = (if (projectMode) allModule.filter { it.isAndroid }.map { it.name } else ModuleRootManager.getInstance(module!!).dependencies.map { it.realName() }).toMutableSet()//过滤Android工程
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
        var envs = mapOf(EnvKeys.toMavenUnCheck to dialog.unCheckCode)

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
                XApp.invoke {
                    GradleUtils.runTask(project, listOf(":${info.title}:ToMaven"), envs = envs, runTaskId = runTaskId, callback = this)
                }
            } else {
                check = false
                runTaskId = ""
                this.onTaskEnd(false, null)//循环运行
            }
        }
    }.onTaskEnd(false, null)
}