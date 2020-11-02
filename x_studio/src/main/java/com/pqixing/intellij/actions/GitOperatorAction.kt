package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.model.ProjectModel
import java.io.File

class GitOperatorAction : AnAction() {
    lateinit var e: AnActionEvent
    lateinit var project: Project
    lateinit var basePath: String

    override fun actionPerformed(e: AnActionEvent) {
        this.e = e
        this.project = e.project ?: return
        this.basePath = project.basePath ?: return

        val codeRoot = XmlHelper.loadConfig(basePath).codeRoot
        val codeRootDir = File(basePath, codeRoot).canonicalPath

        val manifest = XmlHelper.loadManifest(basePath)
        val projects = manifest?.projects?.map { it.copy(path = codeRootDir + it.path) }?.toMutableList() ?: return

        //添加basic工程
        val basicProject = ProjectModel(manifest, "basic", "$basePath/${EnvKeys.BASIC}", "basic", manifest.baseUrl)
        projects.add(0, basicProject)

        val datas = projects.mapIndexed { i, p -> JListInfo(p.name, p.url, 0, i != 0).also { it.data = p } }

    }
}

