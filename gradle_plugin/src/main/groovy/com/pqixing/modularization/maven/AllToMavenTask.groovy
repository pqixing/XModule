package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.dependent.DependentPrintTask
import com.pqixing.modularization.git.CheckMasterTask
import com.pqixing.modularization.utils.FileUtils

class AllToMavenTask extends BaseTask {
    boolean winOs
    File outFile
    StringBuilder outContent

    AllToMavenTask() {
        dependsOn "DependentPrint"
    }

    @Override
    void start() {
        winOs = org.gradle.internal.os.OperatingSystem.current().isWindows()
        outFile = new File(wrapper.getExtends(BuildConfig).outDir, "$Keys.BATH_ALL.${winOs ? "bat" : "sh"}")
        outContent = new StringBuilder("cd ${project.rootDir.absolutePath} \n")
        outContent.append("gradle :${getTaskName(CheckMasterTask)} \n")
    }

    @Override
    void runTask() {
        //所有依赖的文件
        wrapper.getTask(DependentPrintTask).dpBySortList.each {
            writeBathScrip(outContent, it.moduleName)
        }
        if (wrapper.pluginName == Keys.NAME_LIBRARY) writeBathScrip(outContent, project.name)
    }


    @Override
    void end() {
        outContent.append("echo  > $Keys.TXT_HIDE_INCLUDE \n")
        FileUtils.write(outFile, outContent.toString())
    }

    void writeBathScrip(StringBuilder sb, String moduleName) {
        sb.append("echo include = $moduleName > $Keys.TXT_HIDE_INCLUDE \n")
        sb.append("gradle :$moduleName:clean \n")
        sb.append("gradle :$moduleName:ToMaven \n")
    }
}