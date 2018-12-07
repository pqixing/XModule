package com.pqixing.modularization.android.dps

import com.pqixing.modularization.android.dps.DpsExtends.Companion.SCOP_RUNTIME
import com.pqixing.modularization.base.BaseExtension
import org.gradle.api.Project
import java.util.*

/**
 * Created by pqixing on 17-12-25.
 * 依赖的组件
 */

class DpComponents(project: Project) : BaseExtension(project) {

    /**
     * 是否校验过状态
     */
    var check = false
    /**
     * 当前模块是否使用了本地依赖
     */
    var localCompile = false
    var moduleName: String = ""

    var branch = ""
    /**
     * 依赖模式
     * runtimeOnly , compileOnly , implementation , compile
     */
    var scope = SCOP_RUNTIME

    var version: String = ""

    var excludes: LinkedList<Map<String, String>>? = null
    /**
     * 依赖中的依赖树
     */
    var dpComponents: Set<DpComponents>? = null


    private fun checkExclude() {
        if (excludes == null) excludes = LinkedList()
    }

    fun exclude(exclude: Map<String, String>) {
        checkExclude()
        excludes?.add(exclude)
    }

}
