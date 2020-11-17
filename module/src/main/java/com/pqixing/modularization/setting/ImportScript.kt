package com.pqixing.modularization.setting

import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.Module
import com.pqixing.modularization.base.XPlugin
import com.pqixing.modularization.utils.AndroidUtils
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
        setting.rootProject.buildFileName = buildFileName
        val rootBuildFiles = parseBuildFile("\$root", args.env.rootDir.absolutePath)
        FileUtils.mergeFile(File(args.env.rootDir, buildFileName), rootBuildFiles)

        //hook配置的工程的build.gradle,合并原始build.gradle与预设的build.gradle文件,生成新的初始化文件，注入插件进行开发设置
        setting.gradle.beforeProject { pro ->
            //所有项目添加XPlugin依赖
            pro.buildDir = File(pro.buildDir, buildTag)
            pro.pluginManager.apply(XPlugin::class.java)
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

    fun parseBuildFile(f: String, curDir: String): List<File> {
        var file = f.trim()
        val basicDir = args.env.basicDir.absolutePath
        val preFiles = args.manifest.files + mapOf("basicDir" to basicDir)
        val keys = preFiles.keys
        while (true) {
            val key = keys.find { file.contains("$$it") } ?: break
            file = file.replace("$$key", preFiles[key] ?: "")
        }
        return file.split(",").mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } }.map { m ->
            File((curDir.takeIf { !m.startsWith(basicDir) } ?: "") + m)
        }
    }

    /**
     * hook工程的build.gradle文件
     */
    private fun hookBuildFile(module: Module, buildFileName: String) {

        //替换file的模板，重新
//        Tools.println("hookBuildFile: ${module.file} -> $file")
        tryCreateSrc(module)

        val curDir = File(args.env.codeRootDir, module.path).canonicalPath + "/"
        val mergeFiles = parseBuildFile(module.file, curDir).toMutableList()
        val target = File(args.env.codeRootDir, module.path + "/" + buildFileName)

        if (AndroidUtils.buildDev(module, args.runTaskNames)) mergeFiles.add(File(args.env.basicDir, "gradle/dev.gradle"))
        FileUtils.mergeFile(target, mergeFiles) { it.replace(Regex("apply *?plugin: *?['\"]com.android.(application|library)['\"]"), "") }
        module.api?.let { hookBuildFile(it, buildFileName) }
    }

    private fun tryCheckModule(module: Module, buildFileName: String) {
        var mBranch = module.branch()
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
        if (!args.pxApp(module)) module.compiles.removeIf { it.module.api != null }
        if (mBranch.isEmpty()) ResultUtils.thow("clone fail -> ${module.project.url}")

        hookBuildFile(module, buildFileName)
        module.api?.let { tryCheckModule(it, buildFileName) }
    }

    /**
     * 检查代码生成
     */
    private fun tryCreateSrc(module: Module) {
        //非Android工程不生成代码
        if (module.type == "java") createJavaSrc(module)
        if (module.isAndroid) createAndroidSrc(module)
    }

    private fun createJavaSrc(module: Module) {
        //如果build文件存在，不重新生成代码
        val projectDir = File(args.env.codeRootDir, module.path)
        //代码目录
        val sourceDir = File(projectDir, "src/main")
        val buildFile = File(projectDir, "build.gradle")
        if (buildFile.exists()) return

        val name = TextUtils.className(module.name.split("_").joinToString { TextUtils.firstUp(it) })

        val className = "${name}App"
        val groupName = args.manifest.groupId
        val packageName = groupName.replace(".", "/") + "/" + name.toLowerCase(Locale.CHINA)
        FileUtils.writeText(File(sourceDir, "java/$packageName/${className}.java").takeIf { !it.exists() }, "package ${packageName.replace("/", ".")};\nfinal class ${className}{}")

        //如果是application类型，写入build.gradle，并设置applicationId
        FileUtils.writeText(File(projectDir, "build.gradle").takeIf { !it.exists() }, "//java build file")

        Tools.println("   Create src :${module.name} Java ${sourceDir.absolutePath}")
    }

    private fun createAndroidSrc(module: Module) {
        //如果build文件存在，不重新生成代码
        val projectDir = File(args.env.codeRootDir, module.path)
        //代码目录
        val sourceDir = File(projectDir, "src/main")
        val manifest = File(sourceDir, "AndroidManifest.xml")
        if (manifest.exists()) return

        val name = TextUtils.className(module.name.split("_").joinToString { TextUtils.firstUp(it) })
        val groupName = args.manifest.groupId
        val emptyManifest = FileUtils.readText(File(args.env.basicDir, "android/Empty_AndroidManifest.xml"))!!.replace("[groupName]", groupName.toLowerCase()).replace("[projectName]", name.toLowerCase())
        //写入空清单文件
        FileUtils.writeText(manifest, emptyManifest)


        val className = "${name}App"
        val packageName = groupName.replace(".", "/") + "/" + name.toLowerCase(Locale.CHINA)
        FileUtils.writeText(File(sourceDir, "resources/values/strings.xml").takeIf { !it.exists() }, "<resources>\n<string name=\"library_name\">${module.name}</string> \n</resources>")
        FileUtils.writeText(File(sourceDir, "java/$packageName/${className}.java").takeIf { !it.exists() }, "package ${packageName.replace("/", ".")};\nfinal class ${className}{}")

        //如果是application类型，写入build.gradle，并设置applicationId
        if (module.type == Module.TYPE_APP) {
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