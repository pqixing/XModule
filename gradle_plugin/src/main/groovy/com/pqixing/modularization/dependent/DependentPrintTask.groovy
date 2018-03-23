package com.pqixing.modularization.dependent

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.MavenUtils
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
    List<SimpleModule> dpBySortList

    @Override
    void start() {
        buildConfig = wrapper.getExtends(BuildConfig.class)
        outDir = new File(buildConfig.outDir, Keys.DIR_DEPENDENT)
        mavenType = wrapper.getExtends(ModuleConfig).mavenType
        File androidDp =new File(wrapper.getExtends(BuildConfig).outDir, "$Keys.DIR_DEPENDENT/$Keys.FILE_ANDROID_DP")
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
        MavenUtils.getMavenMaps(mavenType.name)?.store(new File(outDir, Keys.FILE_VERSION_DP).newPrintWriter(),Keys.CHARSET)

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
        HashMap<String, SimpleModule> container = new HashMap<>()
        wrapper.getExtends(Dependencies).modules.each { sortDependent(container, it, 1) }

        StringBuilder sb = new StringBuilder("$project.name Sort Dp By Level:\n")
        StringBuilder names = new StringBuilder("\n\nYou can upload all library according to the following order \n")

        dpBySortList = container.values().toList().sort { it.level }

        int curLevel = 0
        dpBySortList.each {
            names.append("$it.moduleName,")

            if (it.level > curLevel) {
                curLevel = it.level
                sb.append("\n$curLevel").append(Keys.TAB)
            }
            sb.append(it.moduleName)
            if (it.type == SimpleModule.TYPE_LOCAL) sb.append(" (local)")
            else if (it.type == SimpleModule.TYPE_BRANCH) sb.append(" ($it.branchName)")
            sb.append(Keys.TAB)
        }
        FileUtils.write(new File(outDir, Keys.FILE_SORT_DP), "${sb.toString()}${names.substring(0, names.length() - 1)}")
    }
    /**
     * 根据依赖层级进行排序根据以下
     * @param container
     * @param module
     * @param level
     */
    void sortDependent(HashMap<String, SimpleModule> container, Module module, int level) {

        def split = module.artifactId.split(Keys.BRANCH_TAG)
        String moduleName = split[0]

        SimpleModule simple = container.get(moduleName)
        if (simple == null) {
            simple = new SimpleModule(moduleName)
            container.put(moduleName, simple)
        }

        int type = SimpleModule.TYPE_MASTER
        if (split.length > 1) {
            simple.branchName = split[1]
            type = SimpleModule.TYPE_BRANCH
        }
        if (module.onLocalCompile) type = SimpleModule.TYPE_LOCAL

        simple.type = Math.max(type, simple.type)
        simple.level = Math.max(level, simple.level)

        module.modules.each { sortDependent(container, it, level + 1) }
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
