package com.pqixing.modularization.android.dps

import com.pqixing.model.Module
import com.pqixing.modularization.android.dps.DpsExtends.Companion.SCOP_RUNTIME

/**
 * Created by pqixing on 17-12-25.
 * 依赖的组件
 */

class DpsModel() {

    /**
     * 该依赖,在配置xml中的信息
     */
    lateinit var module: Module

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

    /**
     * 版本号配置   1  匹配大版本号为1.开头的版本号，  同理 1.0    1.0.0
     */
    var version: String = ""

    /**
     * 如果配置版本号不存在，自动匹配正确的版本号
     */
    var matchAuto = false

    var branch: String = ""

    var justApi = false

    var excludes: HashSet<Pair<String?, String?>> = HashSet()

    override fun toString(): String {
        return "DpComponents(localCompile=$localCompile, moduleName='$moduleName', scope='$scope', version='$version', branch='$branch')"
    }


}
