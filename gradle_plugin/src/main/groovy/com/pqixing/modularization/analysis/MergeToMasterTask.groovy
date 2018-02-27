package com.pqixing.modularization.analysis

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.dependent.DependentPrintTask
import com.pqixing.modularization.utils.FileUtils

class MergeToMasterTask extends BaseTask {
    boolean winOs
    File outFile
    StringBuilder outContent

    MergeToMasterTask() {
        dependsOn "DependentPrint"
    }

    @Override
    void start() {
        winOs = org.gradle.internal.os.OperatingSystem.current().isWindows()
        outFile = new File(wrapper.getExtends(BuildConfig).outDir, "$Keys.BATH_MASTER.${winOs ? "bat" : "sh"}")
        outContent = new StringBuilder("cd ${project.rootDir.absolutePath} \n")
    }

    @Override
    void runTask() {
        //所有依赖的文件test
        wrapper.getTask(DependentPrintTask).dpBySortList.each {
            writeBathScrip(outContent, it.moduleName)
        }
        if (wrapper.pluginName == Keys.NAME_LIBRARY) writeBathScrip(outContent, project.name)
    }


    @Override
    void end() {
        FileUtils.write(outFile, outContent.toString())
        new File(project.rootDir, Keys.TXT_HIDE_INCLUDE).delete()
    }

    void writeBathScrip(StringBuilder sb, String moduleName) {
        sb.append("echo include = $moduleName > $Keys.TXT_HIDE_INCLUDE \n")
        sb.append("gradle :$moduleName:clean \n")
        sb.append("gradle :$moduleName:ToMaven \n")
    }
}