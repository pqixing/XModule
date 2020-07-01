package com.pqixing.modularization.root.tasks

import com.pqixing.modularization.base.BaseTask

/**
 * 同步工程的代码和分支,为了Jekens 构建使用
 */
open class IndexVersionTask : BaseTask() {
    override fun prepare() {
        super.prepare()
        this.dependsOn("uploadArchives")
    }
    override fun runTask() {

    }
}