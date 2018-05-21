package com.pqixing.modularization.android

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.utils.TextUtils

class BuildFirstTask extends BaseTask {
    String type = "Debug"
    String flavorName = ""

    String appName

    void setBuildType(String buildType) {
        type = buildType
        project.android.productFlavors?.each {
            if (flavorName.isEmpty()) flavorName = it.name
            if ("preTest" == it.name) flavorName = it.name
        }
        appName = TextUtils.getSystemEnv(Keys.ENV_BUILD_APP_NAME)?:project.name
        this.dependsOn "assemble${TextUtils.firstUp(flavorName)}$type"
    }

    @Override
    void start() {

    }

    @Override
    void runTask() {
        def path = FileUtils.urls("outputs", "apk", flavorName, type.toLowerCase())
        def name = TextUtils.append("-", project.name, flavorName, type.toLowerCase()) + ".apk"
        def buildFile = new File(project.buildDir, "$path/$name")

        //重命名文件
        if (buildFile.exists()) {
            def outFile = new File(project.buildDir, "$appName-${type}.apk")
            buildFile.renameTo(outFile)
            Print.lnIde("buildApk=${outFile.absolutePath}")
        }
    }

    @Override
    void end() {

    }
}
