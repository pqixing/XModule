package com.pqixing.modularization.dependent

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils
import com.pqixing.modularization.wrapper.PomWrapper
import com.pqixing.modularization.wrapper.ProjectWrapper
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-18.
 * 输出打印依赖
 */

class DependentPrintTask extends BaseTask {
    File outDir
    BuildConfig buildConfig
    MavenType mavenType

    @Override
    void start() {
        buildConfig = wrapper.getExtends(BuildConfig.class)
        outDir = new File(buildConfig.outDir, Keys.DIR_DEPENDENT)
        mavenType = wrapper.getExtends(ModuleConfig).mavenType

        File androidDp = new File(outDir, Keys.FILE_ANDROID_DP)
        project.task(TextUtils.onlyName, type: org.gradle.api.tasks.diagnostics.DependencyReportTask) {
            outputFile = new File(outDir, Keys.FILE_ANDROID_DP)
        }.execute()

        def strList = new LinkedList<String>()
        androidDp.eachLine {
            if (it.startsWith("No dependencies")) {
                strList.removeLast()
                strList.removeLast()
            } else {
                strList.add("$it\n")
            }
        }
        FileUtils.write(androidDp, "$project.name Android Dp : \n$strList")
    }

    @Override
    void runTask() {
        StringBuilder sb = new StringBuilder()
        //输出批量上传脚本以及依赖排序
        wrapper.getExtends(Dependencies).modules.each {
            loadModuleDependent(it)
            writerInnerDp(sb, it, 0)
        }

        FileUtils.write(new File(outDir, Keys.FILE_INNER_DP), "$project.name Inner Dp :\n ${sb.toString()}")
    }

    @Override
    void end() {


    }

    /**
     * 输出依赖文本
     * @param deep
     * @param module
     */
    void writerInnerDp(StringBuilder sb, Module module, int deep) {
        sb.append("${TextUtils.getTab(deep)}++ $module.artifactId : $module.updateTimeStr : ${module.gitLog.replace("\n", "")} \n")
        module.excludes.each { map ->
            sb.append("${TextUtils.getTab(deep + 1)}-- ${map.toString()} \n")
        }
        module.modules.each { writerInnerDp(sb, it, deep + 1) }
    }

    /**
     * 加载该模块的依赖关系
     * @param module
     */
    void loadModuleDependent(Module module) {
        if (module.onLocalCompile) {//如果该模块是本地依赖，则获取对应的project，然后读取其中的依赖
            Project p = wrapper.findProject(module.moduleName)
            if (p != null) {
                module.modules.addAll(ProjectWrapper.with(p).getExtends(Dependencies)?.modules ?: [])
            }
        } else {
            def pomWrapper = PomWrapper.create(mavenType.maven_url, module.groupId, module.artifactId, module.version)
            if (!pomWrapper.empty) pomWrapper.loadModule(module)
        }
        module.modules.each { loadModuleDependent(it) }
    }
}
