package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.pqixing.model.ProjectXmlModel
import groovy.lang.GroovyClassLoader
import groovy.util.XmlParser
import java.io.File

class QToolGroup : DefaultActionGroup() {

    override fun update(e: AnActionEvent?) {
        e?.presentation?.isEnabledAndVisible = true

        initSettingEnable(e?.project)
    }


    companion object {
        var projectConfigs = mutableMapOf<Project, String?>()
        var projectInit = mutableSetOf<Project>()
        fun isModulariztionProject(project: Project?): Boolean {
            project ?: return false
            if (!projectInit.contains(project)) initSettingEnable(project)
            return projectConfigs[project] != null
        }

        fun isDachenProject(project: Project?): Boolean {
            project ?: return false
            if (!projectInit.contains(project)) initSettingEnable(project)
            return isModulariztionProject(project) && projectConfigs[project] == "http://192.168.3.200/android"
        }

        private fun initSettingEnable(project: Project?) {
            val basePath = project?.basePath ?: return
            projectInit.add(project)
            val projectXmlFile = File(basePath, "templet/project.xml")
            if (projectXmlFile.exists()) projectConfigs[project] = XmlParser().parseText(projectXmlFile.readText()).get("@baseUrl").toString()
        }
    }

}
