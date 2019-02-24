package com.pqixing.model

class ProjectXmlModel(val baseUrl: String) {
    val projects = mutableListOf<ProjectModel>()
    fun findSubModuleByName(name: String): SubModule? {
        for (p in projects) {
            for (m in p.submodules) {
                if (name == m.name) return m
            }
        }
        return null
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
            submodules.add(api)
        }
    }
}

data class SubModule(val project: ProjectModel, val name: String, val introduce: String, val path: String, var type: String = SubModuleType.TYPE_LIBRARY) {
    fun getBranch() = project.branch
    fun findApi(): SubModule? {
        if (api == null) api = project.submodules.find { it.name == "${name}_api" }
        return api
    }

    fun isApiModule() = type == SubModuleType.TYPE_LIBRARY_API
    private var api: SubModule? = null
}

object SubModuleType {
    val TYPE_LIBRARY = "library"
    val TYPE_APPLICATION = "application"
    val TYPE_LIBRARY_API = "library_api"
}