package com.pqixing.modularization.android


import com.pqixing.Tools
import com.pqixing.git.Components
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.tools.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.security.Key

open class AndroidPlugin : BasePlugin() {
    override fun callBeforeApplyMould() {
        checkPluginType(project)

        //根据情况进行不同的Android插件依赖
        project.apply(mapOf<String, String>("plugin" to if (BUILD_TYPE == Components.TYPE_APPLICATION) Keys.NAME_APP else Keys.NAME_LIBRARY))
        if (APP_TYPE == Components.TYPE_LIBRARY || APP_TYPE == Components.TYPE_LIBRARY_API) {
            project.apply(mapOf<String, String>("plugin" to "maven"))
        }
    }

    /**
     * 改模块类型， app or library
     */
    var APP_TYPE: String = ""
    /**
     * 编译类型
     */
    var BUILD_TYPE: String = ""

    override val applyFiles: List<String>
        get() {
            if (APP_TYPE == Components.TYPE_APPLICATION) return listOf("com.module.application")
            //如果是独立运行，或者是本地同步时，增加
            if (BUILD_TYPE == Components.TYPE_APPLICATION || BUILD_TYPE == Components.TYPE_LIBRARY_SYNC) return listOf("com.module.library", "com.module.run")
            return listOf("com.module.library")
        }
    override val ignoreFields: Set<String> = setOf("scr/dev")

    override fun linkTask(): List<Class<out Task>> = listOf()

    lateinit var dpsManager: DpsManager
    override fun apply(project: Project) {
        super.apply(project)
        //创建配置读取
        val dpsExt = project.extensions.create(Keys.CONFIG_DPS, DpsExtends::class.java, project)
        val moduleConfig = CompatDps(project, dpsExt)
        project.extensions.add(Keys.CONFIG_MODULE, moduleConfig)

        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        //在工程处理后，处理组件依赖
        extHelper.setExtMethod(project, "endConfig") {
            dpsManager = DpsManager(this@AndroidPlugin)
            val dependencies = dpsManager.resolveDps(dpsExt)
            project.apply(mapOf("from" to FileUtils.writeText(File(cacheDir, FileNames.GRADLE_DEPENDENCIES), dependencies, true)))
        }
        extHelper.setExtValue(project, "ToMavenApi", if (APP_TYPE == Components.TYPE_LIBRARY_API && BUILD_TYPE == Components.TYPE_LIBRARY_API) "Y" else "N")
    }

    /**
     * 检查插件类型
     */
    private fun checkPluginType(project: Project) {
        val components = ProjectManager.findComponent(project.name) ?: return
        APP_TYPE = components.type
        if (APP_TYPE == Components.TYPE_APPLICATION) {
            BUILD_TYPE = Components.TYPE_APPLICATION
            return
        }
        val rxForRun = Regex(":${project.name}:assemble.*?Dev")
        val rxToMaven = ":${project.name}:ToMaven"
        val rxToMavenApi = ":${project.name}:ToMavenApi"
        var match = mutableListOf<String>()
        var assemble = false
        for (t in getGradle().startParameter.taskNames) {
            match.add(t)
            assemble = assemble || t.contains(":assemble")
            when {
                t.matches(rxForRun) -> BUILD_TYPE = Components.TYPE_APPLICATION
                t == rxToMaven -> BUILD_TYPE = Components.TYPE_LIBRARY
                t == rxToMavenApi -> BUILD_TYPE = Components.TYPE_LIBRARY_API
                else -> match.remove(t)
            }
        }
        if (match.size > 1) {
            Tools.printError("Can not run those tasks at times -> $match")
        }
        if (match.size == 0) {
            BUILD_TYPE = if (assemble) Components.TYPE_LIBRARY_LOCAL else Components.TYPE_LIBRARY_SYNC
        }
    }
}
