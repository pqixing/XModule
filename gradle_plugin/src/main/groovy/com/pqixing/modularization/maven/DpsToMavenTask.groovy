package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.dependent.DependentPrintTask
import com.pqixing.modularization.utils.FileUtils

class DpsToMavenTask extends BaseTask {
    boolean winOs
    File outFile
    StringBuilder outContent
    String runGradle

    DpsToMavenTask() {
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
        StringBuilder temp = new StringBuilder("\n$runGradle :CheckBranch :GitUpdate \n")

        outContent.append("echo include=")
        //所有依赖的文件
        def dpBySortList = wrapper.getTask(DependentPrintTask).dpBySortList
        for (int i = dpBySortList.size() - 1; i >= 0; i--) {
            def it = dpBySortList.get(i)
            outContent.append(it.moduleName).append(",")
            writeBathScrip(temp, it.moduleName)
        }
        if (wrapper.pluginName == Keys.NAME_LIBRARY) {
            outContent.append(project.name).append(",")
            writeBathScrip(temp, project.name)
        }

        outContent.append(" > $project.rootDir.absolutePath/$Keys.TXT_HIDE_INCLUDE\n").append(temp)
    }


    @Override
    void end() {
        outContent.append("echo batch upload end > $Keys.TXT_HIDE_INCLUDE \n")
        FileUtils.write(outFile, outContent.toString())
        FileUtils.write(new File(BuildConfig.rootOutDir,outFile.name.replace("all",project.name)), outContent.toString())
    }

    void writeBathScrip(StringBuilder sb, String moduleName) {
        sb.append("echo include = $moduleName > $Keys.TXT_HIDE_INCLUDE \n")
        sb.append("$runGradle :$moduleName:clean :$moduleName:ToMaven \n")
    }
}