package com.pqixing.modularization.android


import com.pqixing.model.SubModule
import com.pqixing.model.SubModuleType
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.android.tasks.BuildApkTask
import com.pqixing.modularization.android.tasks.DpsAnalysisTask
import com.pqixing.modularization.android.tasks.PrepareDevTask
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.ToMavenCheckTask
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
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
        if (buildAsApp && !isApp) extHelper.setApplicationId(project, "${ManagerPlugin.getExtends().docRepoBranch}.${project.name}")
    }

    /**
     * application类型工程
     */
    var isApp = false
    /**
     * 作为app运行  library工程也可以
     */
    var buildAsApp = false

    /**
     * 只是同步工程，不是编译任务
     */
    var justSync = false

    lateinit var subModule: SubModule


    override val applyFiles: List<String>
        get() {
            if (isApp) return listOf("com.module.application")
            //如果是独立运行，或者是本地同步时，包含dev分支
            if (buildAsApp || justSync) return listOf("com.module.library", "com.module.dev")
            return listOf("com.module.library", "com.module.maven")
        }
    override val ignoreFields: Set<String> = emptySet()

    override fun linkTask(): List<Class<out Task>> = mutableListOf(DpsAnalysisTask::class.java, PrepareDevTask::class.java, ToMavenCheckTask::class.java, ToMavenTask::class.java, BuildApkTask::class.java)

    lateinit var dpsManager: DpsManager
    override fun apply(project: Project) {
        this.p = project
        project.extensions.extraProperties.set(project.name, this)
        initSubModule(project)
        //如果是空同步，不做任何处理
        val dpsExt = project.extensions.create(Keys.CONFIG_DPS, DpsExtends::class.java, this, ProjectManager.checkProject(project))
        super.apply(project)
        //创建配置读取
        val moduleConfig = CompatDps(project, dpsExt)
        project.extensions.add(Keys.CONFIG_MODULE, moduleConfig)


        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        //在工程处理后，处理组件依赖
        extHelper.setExtMethod(project, "endConfig") {
            dpsManager = DpsManager(this@AndroidPlugin, dpsExt)
            val dependencies = dpsManager.resolveDps()
            project.apply(mapOf("from" to FileUtils.writeText(File(cacheDir, FileNames.GRADLE_DEPENDENCIES), dependencies, true)))
            compatOldPlugin(dpsExt)
        }
    }

    private fun compatOldPlugin(dpsExt: DpsExtends) {
//
        val javaCacheDir = File(cacheDir, "java")
        val groupName = ManagerPlugin.getExtends().groupName
        val configStr = StringBuilder("package auto.$groupName.${TextUtils.numOrLetter(project.name).toLowerCase()};\n")
                .append("public class ${TextUtils.className(project.name)}Config { \n")
        //Config文件输出
        val DP_CONFIGS_NAMES = dpsExt.compiles.map { "auto.$groupName.${TextUtils.numOrLetter(it.moduleName).toLowerCase()}.${TextUtils.className(it.moduleName)}Config" }
                .sortedBy { it }.toString()
        configStr.append("public static final String  DP_CONFIGS_NAMES = \"${DP_CONFIGS_NAMES.replace("[", "").replace("]", "")}\";\n")
        val CONFIG = "auto.$groupName.${TextUtils.numOrLetter(project.name).toLowerCase()}.${TextUtils.className(project.name)}"
        configStr.append("public static final String  LAUNCH_CONFIG = \"${CONFIG}Launch\";\n")
        val NAME = TextUtils.numOrLetter(project.name).toLowerCase()
        configStr.append("public static final String  NAME = \"$NAME\";\n").append("}")

        val filePath = "auto.$groupName.${TextUtils.numOrLetter(project.name).toLowerCase()}.${TextUtils.className(project.name)}Config".replace(".", "/") + ".java"
        FileUtils.writeText(File(javaCacheDir, filePath), configStr.toString(), true)

        val enterFile = File(javaCacheDir, "auto/com/pqixing/configs/Enter.java")
        if (buildAsApp) {
            val enterStr = StringBuilder("package auto.com.pqixing.configs;\n public class Enter { \n")
                    .append("public static final String  LAUNCH = \"${CONFIG}Launch\";\n")
                    .append("public static final String  CONFIG = \"${CONFIG}Config\";\n")
                    .append("}")
            FileUtils.writeText(enterFile, enterStr.toString(), true)
        } else FileUtils.delete(enterFile)


    }

    /**
     * 检查插件类型
     */
    private fun initSubModule(project: Project) {
        subModule = ProjectManager.projectXml.findSubModuleByName(project.name) ?: return
        isApp = subModule.type == SubModuleType.TYPE_APPLICATION
        if (isApp) {
            buildAsApp = true
            return
        }
        val projectPre = ":${project.name}"
        val filter = getGradle().startParameter.taskNames.filter { it.startsWith(projectPre) }.firstOrNull()
                ?: ""
        if (filter.isEmpty() || filter.matches(Regex(":${project.name}:generate.*?Sources"))) {
            justSync = true
            return
        }
        buildAsApp = filter == ":${project.name}:BuildApk" || filter.matches(Regex(":${project.name}:assemble.*?Dev"))
    }

    companion object {
        fun getPluginByProject(project: Project): AndroidPlugin = project.extensions.extraProperties.get(project.name) as AndroidPlugin
    }
}
