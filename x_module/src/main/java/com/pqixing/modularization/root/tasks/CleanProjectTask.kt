package com.pqixing.modularization.root.tasks

import com.pqixing.help.Tools
import com.pqixing.modularization.base.BaseTask
import com.pqixing.tools.FileUtils
import java.io.File

open class CleanProjectTask : BaseTask() {
    override fun start() {

    }

    override fun runTask() = project.allprojects { p ->
        if (p != p.rootProject) {
            FileUtils.delete(File(p.projectDir, "build"))
            Tools.println("clean ${p.name} ... ")
        }
    }


    override fun end() {

    }
}
