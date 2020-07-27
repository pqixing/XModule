package com.pqixing.modularization.root.tasks

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.root.getArgs

/**
 * 同步工程的代码和分支,为了Jekens 构建使用
 */
open class IndexVersionTask : BaseTask() {
    override fun prepare() {
        super.prepare()
        this.dependsOn("uploadArchives")
    }

    override fun whenReady() {
        super.whenReady()
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        val args =project.getArgs()
        val b ="${args.manifest.groupId}.${args.env.basicBranch}"
        extHelper.setMavenInfo(project, b, "basic", System.currentTimeMillis().toString(), "")
    }
    override fun runTask() {

    }
}