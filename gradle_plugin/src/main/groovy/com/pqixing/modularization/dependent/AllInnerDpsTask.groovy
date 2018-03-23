package com.pqixing.modularization.dependent

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.utils.FileUtils

class AllInnerDpsTask extends BaseTask {
    boolean winOs
    File outFile
    StringBuilder outContent
    String runGradle

    AllInnerDpsTask() {
        dependsOn "DependentPrint"
    }

    @Override
    void start() {
        winOs = org.gradle.internal.os.OperatingSystem.current().isWindows()
        runGradle = winOs?"call gradlew.bat":"./gradlew"
        outFile = new File(wrapper.getExtends(BuildConfig).outDir, "$Keys.BATH_ALL.${winOs ? "bat" : "sh"}")
        outContent = new StringBuilder("cd ${project.rootDir.absolutePath} \n")
//        outContent.append("gradle :${getTaskName(CheckMasterTask)} \n")
    }

    @Override
    void runTask() {

        StringBuilder includeStr = new StringBuilder("include=")
        //所有依赖的文件
        def dpBySortList = wrapper.getTask(DependentPrintTask).dpBySortList
        for (int i = dpBySortList.size() - 1; i >= 0; i--) {
            def it = dpBySortList.get(i)
            includeStr.append(it.moduleName).append(",")
            writeBathScrip(outContent, it.moduleName)
        }
        if (wrapper.pluginName == Keys.NAME_LIBRARY) {
            includeStr.append(project.name).append(",")
            writeBathScrip(outContent, project.name)
        }
        FileUtils.write(new File(project.projectDir,"$BuildConfig.dirName/$Keys.TXT_HIDE_INCLUDE"),includeStr.toString())
    }


    @Override
    void end() {
        outContent.append("echo batch upload end > $Keys.TXT_HIDE_INCLUDE \n")
        FileUtils.write(outFile, outContent.toString())
        FileUtils.write(new File(BuildConfig.rootOutDir,outFile.name.replace("all",project.name)), outContent.toString())
    }

    void writeBathScrip(StringBuilder sb, String moduleName) {
        sb.append("\necho include = $moduleName > $BuildConfig.dirName/$Keys.TXT_HIDE_INCLUDE \n")
        sb.append("$runGradle :CheckBranch :GitUpdate :$moduleName:clean \n")
        sb.append("$runGradle :$moduleName:ToMaven \n")
    }
}