package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.pqixing.model.ProjectXmlModel
import groovy.lang.GroovyClassLoader
import groovy.util.XmlParser
import java.io.File

class QToolGroup : DefaultActionGroup() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val projectXmlFile = File(basePath, "templet/project.xml")
        if (projectXmlFile.exists()) projectConfigs[project] = XmlParser().parseText(projectXmlFile.readText()).get("@baseUrl").toString()
    }


    companion object {
        var projectConfigs = mutableMapOf<Project, String?>()
        fun isModulariztionProject(project: Project?): Boolean {
            return project != null && projectConfigs[project] != null
        }

        fun isDachenProject(project: Project?): Boolean {
            return isModulariztionProject(project) && projectConfigs[project] == "http://192.168.3.200/android"
        }
    }

}
