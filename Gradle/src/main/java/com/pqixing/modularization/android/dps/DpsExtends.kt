package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.git.Components
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.manager.ManagerPlugin
import groovy.lang.Closure
import org.gradle.api.Project
import java.util.*

open class DpsExtends(project: Project,val components:Components) : BaseExtension(project) {
    internal var compiles = HashSet<DpComponents>()
    internal var devCompiles = HashSet<DpComponents>()
    internal var apiCompiles = HashSet<DpComponents>()
    val manager = ManagerPlugin.getExtends()

    /**
     * 上传到Maven的版本
     */
    var toMavenVersion = ""
    get() {
        if(field.isEmpty()) {
            field = ManagerPlugin.getExtends().baseVersion
        }
        return field
    }
    /**
     * 上传到Maven的描述
     */
    var toMavenDesc = ""

    private fun compile(name: String, scope: String = SCOP_COMPILE, container: HashSet<DpComponents>, closure: Closure<Any?>? = null): DpComponents {
        val inner = DpComponents(project)
        //根据 ： 号分割
        val split = name.split(":")
        when (split.size) {
            1 -> {
                inner.branch = components.lastLog.branch
                inner.moduleName = split[0]
                inner.version = "+"
            }
            2 -> {
                inner.branch = components.lastLog.branch
                inner.moduleName = split[0]
                inner.version = split[1]
            }
            3 -> {
                inner.branch = split[0]
                inner.moduleName = split[1]
                inner.version = split[2]
            }
            else -> Tools.printError("DpsExtends compile illegal name -> $name")
        }
        inner.scope = scope
        if (closure != null) {
            closure.delegate = inner
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
        }
        if(inner.version.isEmpty()||inner.version == "+"){
            inner.version = manager.baseVersion
        }
        container.add(inner)
        return inner
    }

    fun compile(moduleName: String) = compile(moduleName, null)
    fun compile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, compiles, closure)
    }

    fun apiCompile(moduleName: String) = apiCompile(moduleName, null)
    fun apiCompile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, apiCompiles, closure)
    }

    fun devCompile(moduleName: String) = devCompile(moduleName, null)
    fun devCompile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, devCompiles, closure).dpType = "dev"
    }

    @Deprecated("Use Api instep", ReplaceWith("api(moduleName, closure)"))
    fun add(moduleName: String) = compile(moduleName)

    @Deprecated("Use Api instep", ReplaceWith("api(moduleName, closure)"))
    fun add(moduleName: String, closure: Closure<Any?>? = null) = compile(moduleName, closure)

    @Deprecated("Use Api instep", ReplaceWith("api(moduleName, closure)"))
    fun addImpl(moduleName: String) = compile(moduleName)

    @Deprecated("Use Api instep", ReplaceWith("api(moduleName, closure)"))
    fun addImpl(moduleName: String, closure: Closure<Any?>? = null) = compile(moduleName, closure)

    companion object {
        val SCOP_API = "api"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILE = "compile"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
