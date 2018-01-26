package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
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
        list += FileUtils.write(new File(buildConfig.cacheDir, "android.gradle"), NormalUtils.parseString(androidTxt, maps))

        if (kotlinEnable)
            list += FileUtils.write(new File(buildConfig.cacheDir, "kotlin.gradle"), NormalUtils.parseString(kotlinTxt, maps))

        if (flavorsEnable && "library" != moduleConfig.pluginType)
            list += FileUtils.write(new File(buildConfig.cacheDir, "flavors.gradle"), NormalUtils.parseString(flavorsTxt, maps))

        Print.ln("android Config generatorFiles $list")
        return list
    }

    String getAndroidTxt() {
        return '''
repositories {
     maven {
         url '#{maven_url}'
     }
     maven {
         url 'http://192.168.3.7:9527/nexus/content/groups/androidgroup/'
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
        multiDexEnabled true
        versionName "#1{versionName}"
        versionCode #1{versionCode}

        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        if(#{compatAppache}&&#{compileSdkVersion}>=23) useLibrary 'org.apache.http.legacy'
        
        if(!#{kotlinEnable}){
            javaCompileOptions {
                annotationProcessorOptions {
                    arguments = [ moduleName : project.getName(), modulePath : project.projectDir.absolutePath,rootPath:project.rootDir.absolutePath]
                }
            }
        }
    }
    
    lintOptions {
          abortOnError false
      }
}
dependencies {
    // 替换成最新版本, 需要注意的是api
    // 要与compiler匹配使用，均使用最新版可以保证兼容
    implementation ('com.alibaba:arouter-api:1.3.0')
    if(!#{kotlinEnable}) annotationProcessor 'com.alibaba:arouter-compiler:1.1.4'
    
    compile 'com.dachen.android:dcannotation:2.7'
   if(!#{kotlinEnable}) annotationProcessor 'com.dachen.android:dccompiler:2.7.2'
    
    implementation "com.android.support:support-annotations:$support_v4"
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.0'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
 }

'''
    }


    String getKotlinTxt() {
        '''
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlin-kapt'
kapt {
    arguments {
        arg("moduleName", project.getName())
        arg("modulePath",  project.projectDir.absolutePath)
        arg("rootPath",  project.rootDir.absolutePath)
    }
}
dependencies {
        kapt 'com.alibaba:arouter-compiler:1.1.4'
        kapt 'com.dachen.android:dccompiler:2.7.2'
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
            manifestPlaceholders += [CHANNEL_NAME: "DACHEN_DOCTOR"]
             dimension "dachen"
        }
        //channelStart----
        yyb {
            manifestPlaceholders += [CHANNEL_NAME: "YINGYONGBAO_DOCTOR"]
            dimension "dachen"
        }
        baidu {
            manifestPlaceholders += [CHANNEL_NAME: "BAIDU_DOCTOR"]
            dimension "dachen"
        }
        wdj {
            manifestPlaceholders += [CHANNEL_NAME: "WANDOUJIA_DOCTOR"]
            dimension "dachen"
        }
        shichang_360 {
            manifestPlaceholders += [CHANNEL_NAME: "360_DOCTOR"]
            dimension "dachen"
        }
        hiapk {
            manifestPlaceholders += [CHANNEL_NAME: "ANZHUO_DOCTOR"]
            dimension "dachen"
        }
        anzhi {
            manifestPlaceholders += [CHANNEL_NAME: "ANZHI_DOCTOR"]
           dimension "dachen"
        }
        mi {
            manifestPlaceholders += [CHANNEL_NAME: "XIAOMI_DOCTOR"]
           dimension "dachen"
        }
        HW {
            manifestPlaceholders += [CHANNEL_NAME: "HUAWEI_DOCTOR"]
          dimension "dachen"
        }
        mz {
            manifestPlaceholders += [CHANNEL_NAME: "MEIZU_DOCTOR"]
            dimension "dachen"
        }
        //channelEnd----
    }
}
'''
    }


    @Override
    public String toString() {
        return "AndroidConfig{" +
                "buildToolsVersion='" + buildToolsVersion + '\'' +
                ", compileSdkVersion='" + compileSdkVersion + '\'' +
                ", minSdkVersion='" + minSdkVersion + '\'' +
                ", targetSdkVersion='" + targetSdkVersion + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", support_v4='" + support_v4 + '\'' +
                ", support_v7='" + support_v7 + '\'' +
                ", kotlinEnable=" + kotlinEnable +
                ", kotlin_version='" + kotlin_version + '\'' +
                ", flavorsEnable=" + flavorsEnable +
                '}';
    }
}
