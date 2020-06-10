package com.pqixing.modularization.android


import com.android.build.gradle.AppExtension
import com.pqixing.Tools
import com.pqixing.model.SubModule
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.IExtHelper
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.android.tasks.BuildApkTask
import com.pqixing.modularization.android.tasks.DpsAnalysisTask
import com.pqixing.modularization.android.tasks.PrepareDevTask
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.manager.allPlugins
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.maven.ToMavenCheckTask
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

open class AndroidPlugin : BasePlugin() {
    override fun callBeforeApplyMould() {

        //根据情况进行不同的Android插件依赖
        project.apply(mapOf<String, String>("plugin" to if (buildAsApp) Keys.NAME_APP else Keys.NAME_LIBRARY))

        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        extHelper.setExtValue(project, "moduleName", project.name)

        if (!isApp) project.apply(mapOf<String, String>("plugin" to "maven"))
        //如果是Library模块运行，设置ApplicationId
        if (buildAsApp && !isApp) {
            extHelper.setApplicationId(project, "com.${TextUtils.letter(project.getArgs().env.templetBranch, "libraryrun")}.${TextUtils.letter(project.name, "app")}".toLowerCase())
        }
    }

    /**
     * application类型工程
     */
    var isApp = false

    /**
     * 作为app运行  library工程也可以
     */
    var buildAsApp = false

    lateinit var subModule: SubModule


    override val applyFiles: List<String>
        get() {
            if (isApp) return listOf("com.module.application")
            val libraryGradle = if (subModule.hasAttach()) "com.module.api" else "com.module.library"
            //如果是独立运行，或者是本地同步时，包含dev分支
            if (buildAsApp) return listOf(libraryGradle, "com.module.dev")
            return listOf(libraryGradle, "com.module.maven")
        }
    override val ignoreFields: Set<String> = emptySet()

    override fun linkTask(): List<Class<out Task>> = mutableListOf(DpsAnalysisTask::class.java, PrepareDevTask::class.java, ToMavenCheckTask::class.java, ToMavenTask::class.java, BuildApkTask::class.java)
    var doAfterList: MutableList<Runnable> = mutableListOf()
    lateinit var dpsManager: DpsManager
    override fun apply(project: Project) {
        this.p = project
        allPlugins[project] = this

        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        extHelper.setExtMethod(project, "doAfterEvaluate") { if (it is Closure<*>) doAfterList.add(it) }
        var processInnerDps: Runnable? = null
        project.afterEvaluate {
            processInnerDps?.run()
            if(processInnerDps==null) Tools.println("--------------error processInnerDps is null-------------- ")
            doAfterList.forEach { c ->
                if (c is Closure<*>) {
                    c.delegate = project
                    c.resolveStrategy = Closure.DELEGATE_ONLY
                }
                c.run()
            }

            project.tasks.find { t -> t.name == "clean" }?.doLast {
                FileUtils.delete(File(project.projectDir, "build"))
            }
        }

        initSubModule(project)
        //如果是空同步，不做任何处理
        val dpsExt = project.extensions.create(Keys.CONFIG_DPS, DpsExtends::class.java, this, ProjectManager.checkProject(project))
        super.apply(project)
        //在工程处理后，处理组件依赖
        processInnerDps = Runnable {
            if (buildAsApp && dpsExt.enableTransform) {
                val android = project.extensions.getByType(AppExtension::class.java)
                //开始注解切入
                android.registerTransform(PqxTransform())
            }

            dpsManager = DpsManager(this@AndroidPlugin, dpsExt)
            val dependencies = dpsManager.resolveDps()
            val dpPath = FileUtils.writeText(File(cacheDir, FileNames.GRADLE_DEPENDENCIES), dependencies, true)
            project.apply(mapOf("from" to dpPath))
            Tools.println("result:$dpPath")
        }
    }

    /**
     * 检查插件类型
     */
    private fun initSubModule(project: Project) {
        subModule = project.getArgs().projectXml.findSubModuleByName(project.name) ?: return
        isApp = subModule.isApplication
        if (isApp) {
            buildAsApp = true
            return
        }
        val projectPre = ":${project.name}"
        val filter = getGradle().startParameter.taskNames.lastOrNull { it.startsWith(projectPre) }
                ?: ""
        buildAsApp = filter == ":${project.name}:BuildApk" || filter.matches(Regex(":${project.name}:assemble.*?Dev"))
    }
}

fun Project.MDPlugin() = allPlugins[project] as AndroidPlugin