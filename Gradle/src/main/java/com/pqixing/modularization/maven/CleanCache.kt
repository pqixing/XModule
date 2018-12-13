package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.modularization.base.BaseTask
import com.pqixing.tools.FileUtils

open class CleanCache : BaseTask() {
    override fun start() {

    }

    override fun runTask() {
        //删除build目录
        FileUtils.delete(project.buildDir)
        Tools.println("${project.name} : CleanCache -> ${project.buildDir.absolutePath}")
    }

    override fun end() {
    }
}