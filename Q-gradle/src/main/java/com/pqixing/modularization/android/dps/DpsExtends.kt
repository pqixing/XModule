package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.modularization.setting.SettingPlugin
import com.pqixing.modularization.setting.ArgsExtends
import groovy.lang.Closure

class DpsExtends(val name: String, val manager: SettingPlugin) {
    internal var compiles = HashSet<DpComponents>()
    internal var devCompiles = HashSet<DpComponents>()
    val args: ArgsExtends = manager.args
    var subModule = args.projectXml.findModule(name)!!
    var enableTransform = true

    init {
        args.dpsContainer[name] = this
    }

    var apiVersion = ""
        get() = if (field.isEmpty()) version else field

    /**
     * 上传到Maven的版本
     */
    var version = ""
        get() = subModule?.attachModule?.let { manager.args.dpsContainer[it.name]?.apiVersion }
                ?: if (field.isEmpty()) manager.args.projectXml.baseVersion else field


    private fun compile(name: String, scope: String = SCOP_COMPILE, container: HashSet<DpComponents>, closure: Closure<Any?>? = null) {
        val inner = DpComponents()
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
            //默认不配置的情况下,使用使用基础版本号
            inner.version = manager.args.projectXml.baseVersion
        }

        inner.matchAuto = inner.version.contains("*")
        inner.version = inner.version.replace("*", "")
        inner.module = manager.args.projectXml.findModule(inner.moduleName)!!

        val apiModule = inner.module.findApi()
        if (apiModule == null) {
            container.add(inner)
            return
        }
        //先尝试加载
        val apiComponents = DpComponents().apply {
            moduleName = apiModule.name
            branch = inner.branch
            version = inner.version
            matchAuto = true
            dpType = inner.dpType
            this.scope = scope
            module = apiModule
        }
        container.add(apiComponents)

        inner.scope = SCOP_RUNTIME
        if (manager.args.runAsApp(subModule) && !inner.justApi) container.add(inner)
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
