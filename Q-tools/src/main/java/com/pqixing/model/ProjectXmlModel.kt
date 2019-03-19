package com.pqixing.model

class ProjectXmlModel(val baseUrl: String) {
    val projects = mutableListOf<ProjectModel>()
    private val maps = HashMap<String, SubModule>()
    fun findSubModuleByName(name: String): SubModule? {
        if (maps.isEmpty()) for (p in projects) {
            for (m in p.submodules) {
                maps[m.name] = m
            }
        }
        return maps[name]
    }

    fun allSubModules() = mutableSetOf<SubModule>().apply {
        projects.forEach { this.addAll(it.submodules) }
    }

    fun findProjectBySubName(name: String) = findSubModuleByName(name)?.project

}

data class ProjectModel(val name: String, val introduce: String, val url: String) {
    var branch: String = ""
    val submodules = mutableListOf<SubModule>()

    fun addSubModule(sm: SubModule) {
        submodules.add(sm)
        if (sm.type == SubModuleType.TYPE_LIBRARY_API) {
            sm.type = SubModuleType.TYPE_LIBRARY
            val api = SubModule(sm.project, "${sm.name}_api", sm.introduce, "${sm.path}/src/api", SubModuleType.TYPE_LIBRARY_API)
            api.parent = sm
            sm.child = api
            submodules.add(api)
        }
    }
}

data class SubModule(val project: ProjectModel, val name: String, val introduce: String, val path: String, var type: String = SubModuleType.TYPE_LIBRARY) {
    var parent:SubModule?=null
    var child:SubModule?=null
    fun getBranch() = project.branch
    fun findApi()=child
    fun findParent()=parent

    fun isApiModule() = type == SubModuleType.TYPE_LIBRARY_API
    private var api: SubModule? = null
    var hasCheck = false
}

object SubModuleType {
    val TYPE_LIBRARY = "library"
    val TYPE_APPLICATION = "application"
    val TYPE_LIBRARY_API = "library_api"
}