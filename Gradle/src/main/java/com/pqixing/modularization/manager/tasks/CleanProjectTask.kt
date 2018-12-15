package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.base.BaseTask

open class CleanProjectTask : BaseTask() {
    override fun start() {

    }

    override fun runTask() = project.allprojects { p ->
        Tools.println("clean ${p.name} ... ")
        p.buildDir.deleteOnExit()
    }


    override fun end() {

    }
}
