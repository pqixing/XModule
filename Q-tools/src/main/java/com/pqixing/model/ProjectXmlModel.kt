package com.pqixing.model

class ProjectXmlModel(val baseUrl: String) {
    var templetUrl = ""

    var mavenUrl = ""
    var mavenGroup = ""
    var mavenUser = ""
    var mavenPsw = ""
    var createSrc = false
    var matchingFallbacks = mutableListOf<String>()
    var baseVersion = "1.0"

    val projects = mutableListOf<ProjectModel>()
    fun findSubModuleByName(name: String): SubModule? {
        for (p in projects) for (m in p.submodules) {
            if (name == m.name) return m
        }
        return null
    }

    fun allSubModules() = mutableSetOf<SubModule>().apply {
        projects.forEach { this.addAll(it.submodules) }
    }
}

data class ProjectModel(val name: String, var path: String, val introduce: String, val url: String) {
    var branch: String = ""
    val submodules = mutableListOf<SubModule>()
}

data class SubModule(val name: String) {
    lateinit var project: ProjectModel
    var path: String = ""
    lateinit var introduce: String
    var isApplication = false

    var type: String = "library"
    var attachModel: SubModule? = null
    var apiModel: SubModule? = null

    //    var apiModel:SubModule?=null//该模块的api模块
    fun getBranch() = project.branch
    fun findApi() = apiModel

    fun hasAttach() = attachModel != null
    var hasCheck = false
}