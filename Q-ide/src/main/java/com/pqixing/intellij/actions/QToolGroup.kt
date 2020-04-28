package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import java.io.File

class QToolGroup : DefaultActionGroup() {

    companion object {

        fun isModulariztionProject(project: Project?): Boolean =project?.baseDir?.findChild("templet")?.findChild("project.xml")?.exists()==true

        fun isDachenProject(project: Project?): Boolean =false
    }

}
