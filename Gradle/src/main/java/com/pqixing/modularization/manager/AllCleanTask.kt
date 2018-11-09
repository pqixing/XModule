package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.modularization.base.BaseTask

open class AllCleanTask : BaseTask() {
    override fun start() {

    }

    override fun runTask() = project.allprojects { p ->
        Tools.println("clean ${p.name} ... ")
        p.buildDir.deleteOnExit()
    }


    override fun end() {

    }
}
