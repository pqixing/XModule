package com.pqixing.modularization.setting

import com.pqixing.Tools
import com.pqixing.model.Module
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.manager.RootPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import java.io.File
import java.util.*

/**
 * 导入脚本
 */
class ImportScript(val args: ArgsExtends, val setting: Settings) {
    val gradle = setting.gradle
    fun startLoad(): String {
        val buildTag = args.config.buildDir?.takeIf { i -> i.isNotEmpty() } ?: "default"
        val buildFileName = "build/${buildTag}/build.gradle"
        //自动抓取工程导入
        val includes = parseInclude()

        val imports = args.projectXml.allModules().filter { includes.contains(it.name) }
        //添加include配置
        val checks = mutableSetOf<Module>()
        for (module in imports) include(checks, module, buildFileName)
        Tools.println("parse include ${args.config.codeRoot} ${imports.map { it.name }}")
        //尝试下载工程
        imports.forEach { tryCheckModule(it) }

        //合并根目录的代码
        setting.rootProject.buildFileName = buildFileName
        FileUtils.mergeFile(File(args.env.rootDir, buildFileName)
                , listOf(File(args.env.basicDir, "build.gradle"), File(args.env.rootDir, "build.gradle")))

        //hook配置的工程的build.gradle,合并原始build.gradle与预设的build.gradle文件,生成新的初始化文件，注入插件进行开发设置
        setting.gradle.beforeProject { pro ->
            if (pro == pro.rootProject) pro.pluginManager.apply(RootPlugin::class.java)
            else {
                pro.buildDir = File(pro.buildDir, buildTag)
                checks.find { it.name == pro.name }?.let { hookBuildFile(pro, it, buildFileName) }
            }
        }
        return buildFileName
    }

    /**
     * hook工程的build.gradle文件
     */
    private fun hookBuildFile(project: Project, module: Module, buildFileName: String) {
        val target = File(args.env.codeRootDir, module.path + "/" + buildFileName)
        val mergeFiles = mutableListOf(File(args.env.codeRootDir, module.path + "/build.gradle"))
        if (module.isAndroid) {
            //尝试生成代码
            tryCreateSrc(project, module)
            //依赖Android插件
            project.pluginManager.apply(AndroidPlugin::class.java)
            mergeFiles += listOf(
                    File(args.env.codeRootDir, module.path + "/build.gradle")
                    , File(args.env.basicDir, "gradle/android.gradle")
                    , File(args.env.basicDir, "gradle/kotlin.gradle")
                    , File(args.env.basicDir, "gradle/maven.gradle"))

            mergeFiles.add(File(args.env.basicDir, "gradle/${if (module.isApplication) "application" else "library"}.gradle"))

            if (module.attach()) mergeFiles.add(File(args.env.basicDir, "gradle/api.gradle"))
            if (args.runAsApp(module) && !module.isApplication) mergeFiles.add(File(args.env.basicDir, "gradle/dev.gradle"))
        }
        FileUtils.mergeFile(target, mergeFiles.reversed()) { it.replace(Regex("apply *?plugin: *?['\"]com.android.(application|library)['\"]"), "") }
    }

    private fun tryCheckModule(module: Module) {
        var mBranch = module.getBranch()
        val projectDir = File(args.env.codeRootDir, module.project.path)

        Tools.println("Check::${module.name}  ${module.path}")
        if (mBranch.isEmpty()) GitUtils.open(projectDir) ?: GitUtils.clone(module.project.url, projectDir)?.let { git ->
            mBranch = git.repository.branch
            module.project.branch = mBranch
            GitUtils.close(git)
        }

        if (mBranch != args.env.basicBranch) {
            Tools.println("   Warming::branch diff $mBranch -> $args.env.basicBranch")
        }
        module.apiModule?.let { tryCheckModule(it) }
    }

    /**
     * 检查代码生成
     */
    private fun tryCreateSrc(pro: Project, module: Module) {
        //非Android工程不生成代码
        if (!module.isAndroid) return
        //如果build文件存在，不重新生成代码
        val projectDir = pro.projectDir
        //代码目录
        val sourceDir = File(projectDir, if (module.attach()) "" else "src/main")
        val manifest = File(sourceDir, "AndroidManifest.xml")
        if (manifest.exists()) return

        val name = TextUtils.className(module.name)
        val groupName = args.projectXml.group
        val emptyManifest = FileUtils.readText(File(args.env.basicDir, "android/Empty_AndroidManifest.xml"))!!.replace("[groupName]", groupName).replace("[projectName]", name)
        //写入空清单文件
        FileUtils.writeText(manifest, emptyManifest)



        val className = "${name}Api"
        val packageName = groupName.replace(".", "/") + "/" + name.toLowerCase(Locale.CHINA)
        FileUtils.writeText(File(sourceDir, "resources/values/strings.xml").takeIf { !it.exists() }, "<resources></resources>")
        FileUtils.writeText(File(sourceDir, "java/$packageName/${className}.java").takeIf { !it.exists() }, "package ${packageName.replace("/", ".")};\nfinal class ${className}{}")

        //如果是application类型，写入build.gradle，并设置applicationId
        if (module.isApplication) {
            FileUtils.writeText(File(projectDir, "build.gradle").takeIf { !it.exists() }, "android {\n    defaultConfig {\n        applicationId '${groupName}.${module.name}'\n    }\n}")
        }

        Tools.println("   Create src :${module.name} ${sourceDir.absolutePath}")
    }

    private fun include(checks: MutableSet<Module>, module: Module, buildFileName: String) {
        setting.include(":" + module.name)
        val pro = setting.findProject(":" + module.name)!!
        pro.projectDir = File(args.env.codeRootDir, module.path)
        pro.buildFileName = buildFileName
        checks.add(module)
        module.apiModule?.let { include(checks, it, buildFileName) }
    }


    fun parseInclude(): MutableSet<String> {
        val includes: MutableSet<String> = (args.config.include?.takeIf { it.isNotEmpty() && it != "Auto" }?.replace("+", ",")?.split(",")
                ?: gradle.startParameter.taskNames.mapNotNull { m -> m.split(":").takeIf { it.size >= 2 }?.let { it[it.size - 2] } }).toSet().toMutableSet()

        val temp = mutableSetOf<String>()
        includes.filter { it.startsWith("D#") }.also { includes.removeAll(it) }.map { it.substring(2) }.forEach { loadAll(temp, it) }
        includes.addAll(temp)

        includes.filter { it.startsWith("E#") }.also { includes.removeAll(it) }.map { it.substring(2) }.forEach { includes.remove(it) }

        temp.clear()
        includes.filter { it.startsWith("ED#") }.also { includes.removeAll(it) }.map { it.substring(3) }.forEach { loadAll(temp, it) }
        includes.removeAll(temp)

        return includes
    }

    fun loadAll(includes: MutableSet<String>, target: String) {
        includes.add(target)

        val requests = args.dpsContainer[target]?.compiles?.map { it.moduleName } ?: return

        val reLoads = requests.filter { !includes.contains(it) }

        //如果已经加载过，不重复加载
        includes.addAll(requests)

        for (request in reLoads) loadAll(includes, request)
    }
}