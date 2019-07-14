package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import java.io.File

class QToolGroup : DefaultActionGroup() {

    companion object {
        var configs = mutableMapOf<Project, Pair<Long, Boolean?>?>()

        fun isModulariztionProject(project: Project?): Boolean {
            project ?: return false
            initSettingEnable(project)
            return configs[project] != null
        }

        fun isDachenProject(project: Project?): Boolean {
            project ?: return false
            initSettingEnable(project)
            return configs[project]?.second == true
        }

        private fun initSettingEnable(project: Project?) {
            val basePath = project?.basePath ?: return
            val projectXmlFile = File(basePath, "templet/project.xml")
            if (!projectXmlFile.exists()) {
                configs[project] = null
            } else if (projectXmlFile.lastModified() > configs[project]?.first ?: 0L) {//距离上次修改有更新时
                configs[project] = Pair(projectXmlFile.lastModified(), projectXmlFile.readText().contains("DachenBase"))
            }
        }
    }

}
