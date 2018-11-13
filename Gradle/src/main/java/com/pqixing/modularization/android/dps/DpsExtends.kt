package com.pqixing.modularization.android.dps

import com.pqixing.modularization.base.BaseExtension
import groovy.lang.Closure

import org.gradle.api.Project

import java.util.HashSet

open class DpsExtends(project: Project) : BaseExtension(project) {
    internal var modules = HashSet<Module>()


    fun module(moduleName: String, scope: String = Module.SCOP_API, closure: Closure<Any?>? = null): Module {
        val inner = Module(project)
        inner.moduleName = moduleName
        inner.scope = scope
        if (closure != null) {
            closure.delegate = inner
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
        }
        modules.add(inner)
        return inner
    }

    fun add(moduleName: String, closure: Closure<Any?>? = null): Module {
        return module(moduleName, Module.SCOP_IMPL, closure)
    }

    fun addImpl(moduleName: String): Module {
        return module(moduleName, Module.SCOP_API, null)
    }

    fun addImpl(moduleName: String, closure: Closure<Any?>?): Module {
        return module(moduleName, Module.SCOP_API, closure)
    }

    companion object {

    }
}
