package com.pqixing.modularization.android


import com.android.build.gradle.AppExtension
import com.pqixing.Tools
import com.pqixing.model.Module
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.android.tasks.BuildApkTask
import com.pqixing.modularization.android.tasks.DpsAnalysisTask
import com.pqixing.modularization.android.tasks.PrepareDevTask
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.setting.SettingPlugin
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

    /**
     * application类型工程
     */
    val isApp:Boolean
        get()= module.isApplication

    /**
     * 作为app运行  library工程也可以
     */
    val buildAsApp:Boolean
        get() = SettingPlugin.findArgs(project).runAsApp(module)

    lateinit var module: Module

    override val ignoreFields: Set<String> = emptySet()

    override fun linkTask(): List<Class<out Task>> = mutableListOf(DpsAnalysisTask::class.java, PrepareDevTask::class.java, ToMavenCheckTask::class.java, ToMavenTask::class.java, BuildApkTask::class.java)
    var doAfterList: MutableList<Runnable> = mutableListOf()
    lateinit var dpsManager: DpsManager
    override fun apply(project: Project) {
        //查找当前项目对应的模块和依赖关系
        val args = SettingPlugin.findArgs(project)
        module = args.projectXml.findModule(project.name)!!
        super.apply(project)
        val dpsExt = args.dpsContainer[module.name]!!
        //如果是空同步，不做任何处理
        extHelper.setExtMethod(project, "doAfterEvaluate") { if (it is Closure<*>) doAfterList.add(it) }
        //根据情况进行不同的Android插件依赖
        project.apply(mapOf<String, String>("plugin" to if (buildAsApp) Keys.NAME_APP else Keys.NAME_LIBRARY))

        //如果是Library模块运行，设置ApplicationId
        if (buildAsApp && !isApp) {
            extHelper.setApplicationId(project, "com.${TextUtils.letter(project.getArgs().env.basicBranch, "libraryrun")}.${TextUtils.letter(project.name, "app")}".toLowerCase())
        }

        project.afterEvaluate {
            if (buildAsApp && dpsExt.enableTransform) {
                val android = project.extensions.getByType(AppExtension::class.java)
                //开始注解切入
                android.registerTransform(PqxTransform())
            }

            dpsManager = DpsManager(this, dpsExt)
            project.apply(mapOf("from" to FileUtils.writeText(File(cacheDir, FileNames.GRADLE_DEPENDENCIES), dpsManager.resolveDps(), true).also {  Tools.println("Depend:$it") }))


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
    }
}

fun Project.pluginModule() = SettingPlugin.findPlugin(this) as AndroidPlugin