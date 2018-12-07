package com.pqixing.modularization.android.dps

import com.pqixing.modularization.base.BaseExtension
import groovy.lang.Closure
import org.gradle.api.Project
import java.util.*

open class DpsExtends(project: Project) : BaseExtension(project) {
    internal var modules = HashSet<DpComponents>()

    private fun compile(moduleName: String, version: String = "+", scope: String = SCOP_COMPILE, closure: Closure<Any?>? = null) {
        val inner = DpComponents(project)
        inner.moduleName = moduleName
        inner.scope = scope
        inner.version = version
        if (closure != null) {
            closure.delegate = inner
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
        }
        modules.add(inner)
        println("compile -> $version $scope")
    }

    fun api(moduleName: String, version: String = "+", closure: Closure<Any?>? = null) = compile(moduleName, version, SCOP_API, closure)

    fun devApi(moduleName: String, version: String = "+", closure: Closure<Any?>? = null) = compile(moduleName, version, SCOP_API, closure)


    fun add(moduleName: String, closure: Closure<Any?>? = null) = add(moduleName, "+", closure)
    fun add(moduleName: String, version: String, closure: Closure<Any?>? = null) = compile(moduleName, version, SCOP_RUNTIME, closure)

    fun addImpl(moduleName: String, closure: Closure<Any?>? = null) = addImpl(moduleName, "+", closure)
    fun addImpl(moduleName: String, version: String, closure: Closure<Any?>? = null) = compile(moduleName, version, SCOP_API, closure)

    companion object {
        val SCOP_API = "api"
        val SCOP_COMPILE = "compile"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
