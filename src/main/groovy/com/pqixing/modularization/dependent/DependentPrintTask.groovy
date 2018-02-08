package com.pqixing.modularization.dependent

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.utils.TextUtils
/**
 * Created by pqixing on 17-12-18.
 * 输出打印依赖
 */

class DependentPrintTask extends BaseTask {
    File outDir
    BuildConfig buildConfig

    DependentPrintTask() {
        buildConfig = wrapper.getExtends(BuildConfig.class)
        outDir = new File(buildConfig.outDir, Keys.DIR_DEPENDENT)
        dependsOn project.task(TextUtils.onlyName, type: org.gradle.api.tasks.diagnostics.DependencyReportTask) {
            outputFile = new File(outDir, Keys.FILE_ANDROID_DP)
        }
    }
    @Override
    void start() {

    }

    @Override
    void runTask() {

    }

    @Override
    void end() {

    }
//    /**
//     * 输出依赖关系
//     * @param project
//     * @return
//     */
//    static void writeDependency(Project project, File outputFile) {
//        def strList = new LinkedList<String>()
//        outputFile.eachLine {
//            if (it.startsWith("No dependencies")) {
//                strList.removeLast()
//                strList.removeLast()
//            } else {
//                strList.add(it + "\\n")
//            }
//        }
//        StringBuilder mapSb = new StringBuilder("-\\n")
//        if (project.localMode) {
//            HashMap<String, Integer> moduleLevels = new HashMap<>()
//            dependencyByLevel(project, moduleLevels, 1)
//            def maps = moduleLevels.toSpreadMap().sort { it.value }
//            mapSb.append("本地工程依赖层级关系: \\n 0 -> $project.name")
//            int curLevel = 0
//            maps.each { map ->
//                if (map.value > curLevel) mapSb.append("\\n $map.value -> ")
//                mapSb.append("$map.key  ")
//                curLevel = map.value
//            }
//            mapSb.append("\\n")
//            ["batch"].each {
//                writePatchUpload(project, maps, new File(project.buildConfig.outDir), it)
//            }
//        }
//        FileUtils.write(outputFile, mapSb.toString())
//        outputFile.append(strList.toString())
//
//
//    }
//    /**
//     * 生成批量上传的脚本
//     * @param maps
//     * @param outDir
//     * @param m
//     */
//    static void writePatchUpload(Project project, Map<String, Integer> maps, File outDir, String envName) {
//        List<String> moduleNames = new LinkedList<>()
//        maps.each { moduleNames.add(0, it.key) }
//
//        StringBuilder sb = new StringBuilder("#!/usr/bin/env bash \\n")
//        sb.append("cd $project.rootDir.absolutePath \\n")
//        moduleNames.each { name ->
//            String taskName = "${name}Upload"
//            sb.append(''' echo "modules+=':#{s1}'"
//    > config2.gradle \ \ n '''.replace("#{s1}", name))
//            sb.append("gradle :$name:updateGit  \\n")
//            sb.append("gradle :$name:clean  \\n")
//            sb.append("gradle :$name:$taskName  \\n")
//            sb.append("sleep 1s  \\n")
//        }
//        write(new File(outDir, "upload${envName}.bat"), sb.toString())
//    }
//
//    static void dependencyByLevel(Project project, HashMap<String, Integer> moduleLevels, int curLevel) {
//        if (curLevel > 10 || project == null || !project.hasProperty("moduleConfig")) return
//        List<String> modulesName = project.moduleConfig.dependModules.moduleNames
//        modulesName.each { moduleLevels.put(it, Math.max(moduleLevels.get(it) ?: 0, curLevel)) }
//        modulesName.each { name ->
//            dependencyByLevel(project.rootProject.findProject(name), moduleLevels, curLevel + 1)
//        }
//    }
}
