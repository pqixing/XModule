package com.pqixing.model

class ManifestModel(val baseUrl: String) {
    var mavenUrl = ""
    var mavenUser = ""
    var mavenPsw = ""
    var groupId = ""
    var createSrc = false
    var matchingFallbacks = mutableListOf<String>()
    var baseVersion = ""

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
}

data class ProjectModel(val manifest: ManifestModel, val name: String, var path: String, val desc: String, val url: String) {
    var branch: String = ""
    val modules = mutableListOf<Module>()
}

data class Module(val name: String, val project: ProjectModel) {
    var path: String = ""
    var desc: String = ""
    val isAndroid: Boolean
        get() = type == "application" || type == "library"
    val isApplication
        get() = type == "application"
    var type: String = ""
    var file = ""
    var attach: Module? = null
    var api: Module? = null
    var node: Any? = null
    var transform = true

    var apiVersion = ""
        get() = if (field.isEmpty()) version else field

    /**
     * 上传到Maven的版本
     */
    var version = ""
        get() = attach?.apiVersion ?: field.takeIf { it.isNotEmpty() } ?: project.manifest.baseVersion


    //    var apiModel:SubModule?=null//该模块的api模块
    fun getBranch() = project.branch
    fun findApi() = api

    fun attach() = attach != null

    val compiles = mutableListOf<Compile>()
    val devCompiles = mutableListOf<Compile>()
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
