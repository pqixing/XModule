package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseTask

open class IndexVersionTask : BaseTask() {
    override fun start() {

    }

    override fun runTask() {
        VersionManager.indexVersionFromNet()
    }

    override fun end() {

    }
}
