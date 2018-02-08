package com.pqixing.modularization.android

import auto.Android
import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class AndroidConfig extends BaseExtension {

    String buildToolsVersion = '26.0.2'
    String compileSdkVersion = '26'
    String minSdkVersion = '16'
    String targetSdkVersion = '21'
    String versionCode
    String versionName
    String applicationId = ""
    String support_v4 = "26.1.0"
    String support_v7 = "26.1.0"
    boolean compatAppache = true

    boolean kotlinEnable = true
    String kotlin_version = "1.2.0"
    FlavorConfig flavor

    void flavor(Closure closure) {
        flavor.configure(closure)
    }

    AndroidConfig(Project project) {
        super(project)
        flavor = new FlavorConfig(project)
    }

    @Override
    LinkedList<String> getOutFiles() {
        def list = []
        ModuleConfig config = wrapper.getExtends(ModuleConfig.class)
        BuildConfig buildConfig = wrapper.getExtends(BuildConfig.class)

        Android file = new Android(properties)

        file.params += ["pluginName": config.runType?.asApp ? Keys.NAME_APP : wrapper.pluginName]
        file.params += ["maven_url": config.mavenType.maven_url]
        file.params += ["mavenGroupUrl": GlobalConfig.mavenGroupUrl]

        //输出android.gradle
        list += FileUtils.write(new File(buildConfig.cacheDir, "android.gradle"), file.androidGradle)
        if (kotlinEnable)
            list += FileUtils.write(new File(buildConfig.cacheDir, "kotlin.gradle"), file.ktolin)
        if (flavor.flavorEnable)
            list += flavor.outFiles
        return list
    }
}
