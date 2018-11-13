package com.pqixing.modularization.android.dps

import com.pqixing.modularization.base.BaseExtension
import org.gradle.api.Project
import java.util.*

/**
 * Created by pqixing on 17-12-25.
 */

class Module(project: Project) : BaseExtension(project) {

    /**
     * 当前模块是否使用了本地依赖
     */
    var onLocalCompile: Boolean = false
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
    var modules: Set<Module>? = null

    fun excludeGroup(groups: Array<String>) {
        if (groups.isEmpty()) return
        checkExclude()
        groups.forEach {
            excludes?.add(mapOf("group" to it))
        }
    }

    private fun checkExclude() {
        if (excludes == null) excludes = LinkedList()
    }

    fun excludeModule(modules: Array<String>) {
        checkExclude()
        modules.forEach {
            excludes?.add(mapOf("module" to it))
        }
    }

    fun exclude(exclude: Map<String, String>) {
        checkExclude()
        excludes?.add(exclude)
    }


    companion object {
        val SCOP_API = "api"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
