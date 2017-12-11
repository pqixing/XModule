package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class AndroidConfig extends BaseExtension {

    String buildToolsVersion = '26.0.2'
    String compileSdkVersion = '26'
    String minSdkVersion = '16'
    String targetSdkVersion = '21'
    String versionCode = '1'
    String versionName = "1.0"
    String applicationId = ""
    String support_v4 = "27.0.1"
    String support_v7 = "27.0.1"

    boolean kotlinEnable = false
    String kotlin_version = "1.2.0"

    boolean flavorsEnable = false

    final Project project

    AndroidConfig(Project project) {
        this.project = project
    }


    @Override
    LinkedList<String> generatorFiles() {
        def list = []
        ModuleConfig moduleConfig = project.moduleConfig
        BuildConfig buildConfig = project.buildConfig
        MavenType mavenType = moduleConfig.mavenType

        def maps = ["androidPlugin": moduleConfig.compilePluginType]
        maps.putAll(properties)
        maps.put("maven_url", mavenType?.maven_url)
        //输出android.gradle
        list += FileUtils.write(new File(buildConfig.cacheDir, "android.gradle"),
                NormalUtils.parseString(androidTxt, maps))
        if (kotlinEnable) list += FileUtils.write(new File(buildConfig.cacheDir, "kotlin.gradle"),
                NormalUtils.parseString(kotlinTxt, maps))
        if (flavorsEnable) list += FileUtils.write(new File(buildConfig.cacheDir, "flavors.gradle"),
                NormalUtils.parseString(flavorsTxt, maps))

        println("android Config generatorFiles $list")
        return list
    }

    String getAndroidTxt() {
        return '''
repositories {
     maven {
         url #{maven_url}
     }
}

apply plugin: "com.android.#{androidPlugin}"
android {
    buildToolsVersion '#{buildToolsVersion}'
    compileSdkVersion #{compileSdkVersion}
    defaultConfig {
        applicationId '#1{applicationId}'
        minSdkVersion #{minSdkVersion}
        targetSdkVersion #{targetSdkVersion}

        versionName "#1{versionName}"
        versionCode #1{versionCode}

        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        manifestPlaceholders = [CHANNEL_NAME: "DACHEN_DOCTOR"]
    }
}
'''
    }


    String getKotlinTxt() {
        '''
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
dependencies {
        implementation "org.jetbrains.kotlin:kotlin-stdlib-jre7:#{kotlin_version}"
 }
'''
    }

    String getFlavorsTxt() {
        return '''
//多渠道打包
android{
    flavorDimensions "dachen"
    productFlavors {
        dachen {
            manifestPlaceholders = [CHANNEL_NAME: "DACHEN_DOCTOR"]
             dimension "dachen"
        }
        //channelStart----
        yyb {
            manifestPlaceholders = [CHANNEL_NAME: "YINGYONGBAO_DOCTOR"]
            dimension "dachen"
        }
        baidu {
            manifestPlaceholders = [CHANNEL_NAME: "BAIDU_DOCTOR"]
            dimension "dachen"
        }
        wdj {
            manifestPlaceholders = [CHANNEL_NAME: "WANDOUJIA_DOCTOR"]
            dimension "dachen"
        }
        shichang_360 {
            manifestPlaceholders = [CHANNEL_NAME: "360_DOCTOR"]
            dimension "dachen"
        }
        hiapk {
            manifestPlaceholders = [CHANNEL_NAME: "ANZHUO_DOCTOR"]
            dimension "dachen"
        }
        anzhi {
            manifestPlaceholders = [CHANNEL_NAME: "ANZHI_DOCTOR"]
           dimension "dachen"
        }
        mi {
            manifestPlaceholders = [CHANNEL_NAME: "XIAOMI_DOCTOR"]
           dimension "dachen"
        }
        HW {
            manifestPlaceholders = [CHANNEL_NAME: "HUAWEI_DOCTOR"]
          dimension "dachen"
        }
        mz {
            manifestPlaceholders = [CHANNEL_NAME: "MEIZU_DOCTOR"]
            dimension "dachen"
        }
        //channelEnd----
    }
}
'''
    }
}
