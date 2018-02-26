package com.pqixing.modularization.analysis

import auto.Android
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.dependent.DependentPrintTask
import com.pqixing.modularization.utils.FileUtils

class MergeToReleaseTask extends BaseTask {
    boolean winOs
    File outFile
    StringBuilder outContent
    String releaseGradle

    MergeToReleaseTask() {
        dependsOn "DependentPrint"
    }

    @Override
    void start() {
        winOs = org.gradle.internal.os.OperatingSystem.current().isWindows()
        outFile = new File(wrapper.getExtends(BuildConfig).outDir, "$Keys.BATH_RELEASE.${winOs ? "bat" : "sh"}")
        outContent = new StringBuilder("cd ${project.rootDir.absolutePath} \n")
        releaseGradle = new Android().releaseGradle.replace("\n", "")
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
        FileUtils.write(outFile, outContent.toString())
        new File(project.rootDir, Keys.TXT_HIDE_INCLUDE).delete()
        new File(project.rootDir, Keys.FOCUS_GRADLE).delete()
    }

    void writeBathScrip(StringBuilder sb, String moduleName) {
        sb.append("echo $releaseGradle > $Keys.FOCUS_GRADLE \n")
        sb.append("echo include = $moduleName > $Keys.TXT_HIDE_INCLUDE \n")
        sb.append("gradle :$moduleName:clean \n")
        sb.append("gradle :$moduleName:ToMaven \n")
    }
}