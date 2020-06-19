package com.pqixing.model

class ProjectXmlModel(val baseUrl: String) {
    var basicUrl = ""

    var mavenUrl = ""
    var mavenUser = ""
    var mavenPsw = ""
    var group = ""
    var createSrc = false
    var matchingFallbacks = mutableListOf<String>()
    var baseVersion = "1.0"

    val projects = mutableListOf<ProjectModel>()
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

data class ProjectModel(val name: String, var path: String, val introduce: String, val url: String) {
    var branch: String = ""
    val modules = mutableListOf<Module>()
}

data class Module(val name: String) {
    lateinit var project: ProjectModel
    var path: String = ""
    lateinit var introduce: String
    val isAndroid:Boolean
        get() = type == "application" ||type== "library"
    val isApplication
        get() = type == "application"
    var type: String = ""
    var attachModule: Module? = null
    var apiModule: Module? = null

    //    var apiModel:SubModule?=null//该模块的api模块
    fun getBranch() = project.branch
    fun findApi() = apiModule

    fun attach() = attachModule != null
}