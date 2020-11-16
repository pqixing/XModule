package com.pqixing.modularization.setting

import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.Module
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.base.XPlugin
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
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
        val buildFileName = "build/$buildTag/build.gradle"

        //自动抓取工程导入
        val includes = XmlHelper.parseInclude(args.manifest, formatIncludeTxt(args.config.include.trim()))

        val imports = args.manifest.allModules().filter { includes.contains(it.name) }
        //添加include配置
        val checks = mutableSetOf<Module>()
        for (module in imports) include(checks, module, buildFileName)
        Tools.println("Import ${args.config.codeRoot}  ${args.config.include} -> ${imports.map { it.name }}")
        //尝试下载工程,hook build.gradle文件
        imports.forEach { tryCheckModule(it, buildFileName) }

        //合并根目录的代码
        setting.rootProject.buildFileName = "build/build.gradle"
        FileUtils.mergeFile(File(args.env.rootDir, "build/build.gradle"), listOf(File(args.env.basicDir, "build.gradle"), File(args.env.rootDir, "build.gradle"), File(args.env.basicDir, "gradle/maven.gradle")))

        //hook配置的工程的build.gradle,合并原始build.gradle与预设的build.gradle文件,生成新的初始化文件，注入插件进行开发设置
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        setting.gradle.beforeProject { pro ->

            //所有项目添加XPlugin依赖
            pro.pluginManager.apply(XPlugin::class.java)
            extHelper.setExtValue(pro, "basicDir", args.env.basicDir.canonicalPath)
            extHelper.setExtValue(pro, "basicUrl", args.config.basicUrl)
            checks.find { it.name == pro.name }?.let { module ->
                pro.buildDir = File(pro.buildDir, buildTag)
                //依赖Android插件
                if (module.isAndroid) pro.pluginManager.apply(AndroidPlugin::class.java)
            }
        }
        return buildFileName
    }


    fun formatIncludeTxt(source: String): Set<String> {
        if (source.isNotEmpty() && source != "Auto") {
            return source.replace("+", ",")?.split(",").toSet()
        }
        val result = gradle.startParameter.taskNames.map { it.split(":") }.flatten().filter { it.isNotEmpty() }.toSet()

        Tools.println("formatIncludeTxt : ${gradle.startParameter.taskNames}  -> $result")
        return result
    }

    /**
     * hook工程的build.gradle文件
     */
    private fun hookBuildFile(module: Module, buildFileName: String) {

        //替换file的模板，重新
        var file = module.file
        val basicDir = args.env.basicDir.absolutePath
        val curDir = File(args.env.codeRootDir, module.path).canonicalPath + "/"

        val preFiles = args.manifest.files + mapOf("basicDir" to basicDir)
        val keys = preFiles.keys
        while (true) {
            val key = keys.find { file.contains("$$it") } ?: break
            file = file.replace("$$key", preFiles[key] ?: "")
        }
//        Tools.println("hookBuildFile: ${module.file} -> $file")

        tryCreateSrc(module)


        val target = File(args.env.codeRootDir, module.path + "/" + buildFileName)

        val mergeFiles = file.split(",").mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } }.map { m ->
            File((curDir.takeIf { !m.startsWith(basicDir) } ?: "") + m)
        }.toMutableList()

        if (module.attach()) mergeFiles.add(File(args.env.basicDir, "gradle/api.gradle"))
        if (args.runAsApp(module) && !module.isApplication) mergeFiles.add(File(args.env.basicDir, "gradle/dev.gradle"))
        FileUtils.mergeFile(target, mergeFiles) { it.replace(Regex("apply *?plugin: *?['\"]com.android.(application|library)['\"]"), "") }
        module.api?.let { hookBuildFile(it, buildFileName) }
    }

    private fun tryCheckModule(module: Module, buildFileName: String) {
        var mBranch = module.getBranch()
        val projectDir = File(args.env.codeRootDir, module.project.path)

//        Tools.println("Check::${module.name}  ${args.env.codeRootDir.absolutePath}${module.path}")
        if (mBranch.isEmpty()) (GitUtils.open(projectDir)
                ?: GitUtils.clone(module.project.url, projectDir))?.let { git ->
            mBranch = git.repository.branch
            module.project.branch = mBranch

            GitUtils.close(git)
        }

        //重新设置依赖的分支
        module.compiles.forEach { if (it.branch.isEmpty()) it.branch = mBranch }
        module.devCompiles.forEach { if (it.branch.isEmpty()) it.branch = mBranch }
        //非application工程，移除对于设置了api工程的依赖
        if (!args.runAsApp(module)) module.compiles.removeIf { it.module.api != null }
        if (mBranch.isEmpty()) ResultUtils.thow("clone fail -> ${module.project.url}")

        hookBuildFile(module, buildFileName)
        module.api?.let { tryCheckModule(it, buildFileName) }
    }

    /**
     * 检查代码生成
     */
    private fun tryCreateSrc(module: Module) {
        //非Android工程不生成代码
        if (!module.isAndroid) return
        //如果build文件存在，不重新生成代码
        val projectDir = File(args.env.codeRootDir, module.path)
        //代码目录
        val sourceDir = File(projectDir, if (module.attach()) "" else "src/main")
        val manifest = File(sourceDir, "AndroidManifest.xml")
        if (manifest.exists()) return

        val name = TextUtils.className(module.name.split("_").joinToString { TextUtils.firstUp(it) })
        val groupName = args.manifest.groupId
        val emptyManifest = FileUtils.readText(File(args.env.basicDir, "android/Empty_AndroidManifest.xml"))!!.replace("[groupName]", groupName.toLowerCase()).replace("[projectName]", name.toLowerCase())
        //写入空清单文件
        FileUtils.writeText(manifest, emptyManifest)


        val className = if (module.attach()) name else "${name}App"
        val packageName = groupName.replace(".", "/") + "/" + name.toLowerCase(Locale.CHINA)
        if (!module.attach()) FileUtils.writeText(File(sourceDir, "resources/values/strings.xml").takeIf { !it.exists() }, "<resources>\n<string name=\"library_name\">${module.name}</string> \n</resources>")
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
        module.api?.let { include(checks, it, buildFileName) }
    }

}