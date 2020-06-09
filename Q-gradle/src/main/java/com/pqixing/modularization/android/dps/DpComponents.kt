package com.pqixing.modularization.android.dps

import com.pqixing.model.SubModule
import com.pqixing.modularization.android.dps.DpsExtends.Companion.SCOP_RUNTIME
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.manager.ProjectManager
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 * 依赖的组件
 */

class DpComponents(project: Project) : BaseExtension(project) {

    /**
     * 该依赖,在配置xml中的信息
     */
    lateinit var subModule: SubModule

    /**
     * 当前模块是否使用了本地依赖
     */
    var localCompile = false
    var moduleName: String = ""

    var dpType = ""

    /**
     * 依赖模式
     * runtimeOnly , compileOnly , implementation , compile
     */
    var scope = SCOP_RUNTIME

    var version: String = ""

    var emptyVersion = false

    var branch: String = ""

    var justApi = false

    var apiVersion = ""
        get() = if (field.isEmpty()) version else field

    var excludes: HashSet<Pair<String?, String?>> = HashSet()

    override fun toString(): String {
        return "DpComponents(localCompile=$localCompile, moduleName='$moduleName', scope='$scope', version='$version', branch='$branch')"
    }


}
