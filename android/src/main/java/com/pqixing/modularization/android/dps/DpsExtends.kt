package com.pqixing.modularization.android.dps

import com.pqixing.Tools
import com.pqixing.modularization.setting.ArgsExtends
import groovy.lang.Closure

class DpsExtends(val name: String, val args: ArgsExtends) {
    internal var compiles = HashSet<DpsModel>()
    internal var devCompiles = HashSet<DpsModel>()
    var subModule = args.projectXml.findModule(name)!!
    var enableTransform = true
    var hadConfig = false

    init {
        args.dpsContainer[name] = this
    }

    var apiVersion = ""
        get() = if (field.isEmpty()) version else field

    /**
     * 上传到Maven的版本
     */
    var version = ""
        set(value) {
            hadConfig = true
            field = value
        }
        get() = subModule.attachModule?.let { args.dpsContainer[it.name]?.apiVersion }
                ?: if (field.isEmpty()) args.projectXml.baseVersion else field


    private fun compile(name: String, scope: String = SCOP_COMPILE, container: HashSet<DpsModel>, closure: Closure<Any?>? = null) {
        if (name.isEmpty()) return
        val inner = DpsModel()
        //根据 ： 号分割
        val split = name.split(":")
        when (split.size) {
            1 -> {
                inner.branch = subModule.getBranch()
                inner.name = split[0]
                inner.version = ""
            }
            2 -> {
                inner.branch = subModule.getBranch()
                inner.name = split[0]
                inner.version = split[1]
            }
            3 -> {
                inner.branch = split[0]
                inner.name = split[1]
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

        inner.matchAuto = inner.version.contains("*")
        inner.version = inner.version.replace("*", "")
        inner.module = args.projectXml.findModule(inner.name) ?: return

        val apiModule = inner.module.findApi()
        if (apiModule == null) {
            container.add(inner)
            return
        }
        //先尝试加载
        val api = DpsModel().apply {
            this.name = apiModule.name
            branch = inner.branch
            version = ""
            matchAuto = true
            attach = inner
            dpType = inner.dpType
            this.scope = scope
            module = apiModule
        }
        container.add(api)

        inner.scope = SCOP_RUNTIME
        if (args.runAsApp(subModule) && !inner.justApi) container.add(inner)
    }

    fun compile(moduleName: String) = compile(moduleName, null)
    fun compile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, compiles, closure)
    }


    fun devCompile(moduleName: String) = devCompile(moduleName, null)
    fun devCompile(moduleName: String, closure: Closure<Any?>? = null) {
        compile(moduleName, SCOP_API, devCompiles)
    }

    companion object {
        val SCOP_API = "api"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILE = "compile"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
