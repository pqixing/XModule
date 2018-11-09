package com.pqixing.modularization.android.dps

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.tools.CheckUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Project
import java.util.*

/**
 * Created by pqixing on 17-12-25.
 */

class Module(project: Project) : BaseExtension(project) {

    //强制使用master分支
    var focusMaster = false
    /**
     * 当前模块是否使用了本地依赖
     */
    var onLocalCompile: Boolean = false
    var moduleName: String? = null
    var artifactId: String? = null
        get() {
            if (CheckUtils.isEmpty(field)) field = moduleName
            return TextUtils.removeLineAndMark(field!!)
        }


    /**
     * 依赖模式
     * runtimeOnly , compileOnly , implementation , compile
     */
    var scope = SCOP_RUNTIME

    var version: String? = null
    /**
     * 最后更新时间
     */
    var updateTime: Long = 0
    /**
     * 更新说明
     */
    var gitLog = ""
    var excludes = LinkedList<Map<String, String>>()
    /**
     * 依赖中的依赖树
     */
    var modules: Set<Module> = HashSet()

    val updateTimeStr: String
        get() = Date(updateTime).toLocaleString()

    fun excludeGroup(groups: Array<String>) {
        groups.forEach {
            excludes.add(mapOf("group" to it))
        }
    }

    fun excludeModule(modules: Array<String>) {
        modules.forEach {
            excludes.add(mapOf("module" to it))
        }
    }

    fun exclude(exclude: Map<String, String>) {
        excludes.add(exclude)
    }


    override fun toString(): String {
        return "Module{" +
                "onLocalCompile=" + onLocalCompile +
                ", moduleName='" + moduleName + '\''.toString() +
                ", artifactId='" + artifactId + '\''.toString() +
                ", scope='" + scope + '\''.toString() +
                ", version='" + version + '\''.toString() +
                ", updateTime=" + updateTime +
                ", gitLog='" + gitLog + '\''.toString() +
                '}'.toString()
    }

    companion object {
        val SCOP_API = "api"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
