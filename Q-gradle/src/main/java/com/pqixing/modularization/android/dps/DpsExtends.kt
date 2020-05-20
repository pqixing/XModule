package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.model.SubModule
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.android.MDPlugin
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.tools.TextUtils
import groovy.lang.Closure

open class DpsExtends(val plugin: AndroidPlugin, val subModule: SubModule) : BaseExtension(plugin.project) {
    internal var compiles = HashSet<DpComponents>()
    internal var devCompiles = HashSet<DpComponents>()
    val manager = ManagerPlugin.getExtends()
    var enableTransform = true

    /**
     * 上传到Maven的版本
     */
    var toMavenVersion = ""
        get() = if (subModule.isApiModule()) {
            val n = subModule.parent!!.name
            val dpsExt = plugin.project.rootProject.findProject(n)!!.MDPlugin().dpsManager.dpsExt
            dpsExt.toMavenVersion
        } else {
            if (field.isEmpty()) {
                field = ManagerPlugin.getExtends().baseVersion
            }
            field
        }
    /**
     * 上传到Maven的描述
     */
    var toMavenDesc = ""
        get() = if (subModule.isApiModule()) {
            val n = subModule.parent!!.name
            val dpsExt = plugin.project.rootProject.findProject(n)!!.MDPlugin().dpsManager.dpsExt
            dpsExt.toMavenDesc
        } else {
            val desc = TextUtils.getSystemEnv("toMavenDesc")
            if (desc?.isNotEmpty() == true) desc else field
        }


    private fun compile(name: String, scope: String = SCOP_COMPILE, container: HashSet<DpComponents>, closure: Closure<Any?>? = null) {
        val inner = DpComponents(project)
        //根据 ： 号分割
        val split = name.split(":")
        when (split.size) {
            1 -> {
                inner.branch = subModule.getBranch()
                inner.moduleName = split[0]
                inner.version = ""
            }
            2 -> {
                inner.branch = subModule.getBranch()
                inner.moduleName = split[0]
                inner.version = split[1]
            }
            3 -> {
                inner.branch = split[0]
                inner.moduleName = split[1]
                inner.version = split[2]
            }
            else -> Tools.printError(-1, "DpsExtends compile illegal name -> $name")
        }
        inner.scope = scope
        if (closure != null) {
            closure.delegate = inner
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
        }
        if (inner.version.isEmpty()) {
//            inner.version = manager.baseVersion
            //默认不配置的情况下,使用最新的版本号
            inner.emptyVersion = true
            inner.version = "+"
        }
        inner.subModule = ProjectManager.findSubModuleByName(inner.moduleName)!!
        val apiModule = inner.subModule.findApi()
        if (apiModule == null) {
            container.add(inner)
            return
        }
        val apiComponents = DpComponents(plugin.project).apply {
            moduleName = apiModule.name
            branch = inner.branch
            version = inner.version
            dpType = inner.dpType
            this.scope = scope
            subModule = apiModule
        }
        inner.scope = DpsExtends.SCOP_RUNTIME
        container.add(apiComponents)
        if (plugin.buildAsApp) container.add(inner)
    }

    fun compile(moduleName: String) = compile(moduleName, null)
    fun compile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, compiles, closure)
    }


    fun devCompile(moduleName: String) = devCompile(moduleName, null)
    fun devCompile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, devCompiles)
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
