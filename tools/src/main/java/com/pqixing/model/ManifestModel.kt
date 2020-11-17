package com.pqixing.model

import com.pqixing.tools.TextUtils

class ManifestModel(val baseUrl: String) {
    var mavenUrl = ""
    var mavenUser = ""
    var mavenPsw = ""
    var groupId = ""
    var createSrc = false
    var fallbacks = mutableListOf<String>()
    var baseVersion = ""
    var basicUrl = ""


    val projects = mutableListOf<ProjectModel>()

    val files = mutableMapOf<String, String>()
    fun findModule(name: String): Module? {
        for (p in projects) for (m in p.modules) {
            if (name == m.name) return m
        }
        return null
    }

    fun allModules() = mutableSetOf<Module>().apply {
        projects.forEach { this.addAll(it.modules) }
    }

    fun fullUrl(vararg names: String = emptyArray()) = TextUtils.append(arrayOf(mavenUrl, groupId.replace(".", "/")).plus(names))
}

data class ProjectModel(val manifest: ManifestModel, val name: String, var path: String, val desc: String, val url: String) {
    var branch: String = ""
    val modules = mutableListOf<Module>()
}

data class Module(val name: String, val project: ProjectModel) {
    companion object {
        const val TYPE_APP = "application"
        const val TYPE_LIB = "library"
        const val TYPE_JAVA = "java"
        const val TYPE_DOC = "document"
    }

    var path: String = ""
    var desc: String = ""
    val isAndroid: Boolean
        get() = type == TYPE_APP || type == TYPE_LIB
    val forMaven: Boolean
        get() = type == TYPE_LIB || type == TYPE_JAVA
    val forDps: Boolean
        get() = isAndroid || forMaven
    var type: String = ""
    var file = ""
    var api: Module? = null
    var node: Any? = null

    var apiVersion = ""
        get() = field.takeIf { it.isNotEmpty() } ?: api?.version ?: version

    /**
     * 上传到Maven的版本
     */
    var version = ""
        get() = field.takeIf { it.isNotEmpty() } ?: project.manifest.baseVersion


    //    var apiModel:SubModule?=null//该模块的api模块
    fun branch() = project.branch
    fun findApi() = api

    val compiles = mutableListOf<Compile>()
    val devCompiles = mutableListOf<Compile>()

    fun allCompiles(self: Boolean = true): Set<String> = mutableSetOf<String>().also { loadCompiles(it, this);if (self) it.add(name) }
    private fun loadCompiles(all: MutableSet<String>, module: Module) {
        if (!all.add(module.name)) return
        for (c in module.compiles) {
            loadCompiles(all, c.module)
        }
    }
}

/**
 * Created by pqixing on 17-12-25.
 * 依赖的组件
 */

class Compile(val module: Module) {

    /**
     * 该依赖,在配置xml中的信息
     */

    /**
     * 当前模块是否使用了本地依赖
     */
    var local = false

    val name: String = module.name

    var dpType = ""

    /**
     * 依赖模式
     * runtimeOnly , compileOnly , implementation , compile
     */
    var scope = SCOP_RUNTIME

    /**
     * 版本号配置   1  匹配大版本号为1.开头的版本号，  同理 1.0    1.0.0
     * 如果为空，则默认使用对应模块在manifest文件中配置的版本号
     */
    var version: String = ""
        get() = field.takeIf { it.isNotEmpty() } ?: module.version

    var branch: String = ""

    var justApi = false

    var excludes: HashSet<Pair<String?, String?>> = HashSet()

    var attach: Compile? = null


    companion object {
        val SCOP_API = "api"
        val SCOP_RUNTIME = "runtimeOnly"
        val SCOP_COMPILE = "compile"
        val SCOP_COMPILEONLY = "compileOnly"
        val SCOP_IMPL = "implementation"
    }
}
