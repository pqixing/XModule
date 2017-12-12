package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils

/**
 * Created by pqixing on 17-12-7.
 */

class RunType extends BaseContainerExtension {
    boolean asApp = false
    String applicationLike
    String launchActivity
    String app_icon
    String app_name
    String app_theme


    RunType(String name) {
        super(name)
    }


    @Override
    public String toString() {
        return "RunType{" +
                "asApp=" + asApp +
                ", applicationLike='" + applicationLike + '\'' +
                ", launchActivity='" + launchActivity + '\'' +
                ", app_icon='" + app_icon + '\'' +
                ", app_name='" + app_name + '\'' +
                ", app_theme='" + app_theme + '\'' +
                '}';
    }

    @Override
    LinkedList<String> generatorFiles() {
        if ("application" == project.moduleConfig.pluginType || !asApp) return ""//不独立运行，不生产缓存类文件

        BuildConfig buildConfig = project.buildConfig
        def maps = properties
        maps.putAll(buildConfig.properties)

        //输出Application类
        def applicationFile = new File(FileUtils.appendUrls(buildConfig.cacheDir, "java"
                , buildConfig.packageName.replace('.', File.separator), "DefaultAppCation.java"))
        FileUtils.write(applicationFile, NormalUtils.parseString(applicationTxt, maps))

        //输出Activity类
        def activityFile = new File(FileUtils.appendUrls(buildConfig.cacheDir, "java"
                , buildConfig.packageName.replace('.', File.separator), "DefaultActivity.java"))
        FileUtils.write(activityFile, NormalUtils.parseString(activityTxt, maps))

        //输出临时清单文件
        def inputManifest = new File(FileUtils.appendUrls(project.projectDir.path, "src", "main"), "AndroidManifest.xml").text
        inputManifest = inputManifest.replaceFirst("<manifest(?s).*?>", NormalUtils.parseString(manifestMetaTxt, maps))
                .replaceFirst("<application(?s).*?>", NormalUtils.parseString(manifestAppTxt, maps))
        FileUtils.write(new File(buildConfig.cacheDir, "AndroidManifest.xml"), inputManifest)

        //输出source的gradle配置
        return [FileUtils.write(new File(buildConfig.cacheDir, "sourceSets.gradle"), NormalUtils.parseString(sourceSetTxt, ["cacheDir": buildConfig.cacheDir]))]
    }

    String getManifestMetaTxt() {
        return '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="#{packageName}"
    android:versionCode="#{versionCode}"
    android:versionName="#{versionName}"
    >
'''
    }

    String getManifestAppTxt() {
        return '''
<application
        android:allowBackup="true"
        android:name="#{packageName}.DefaultAppCation"
        android:icon="#{app_icon}"
        android:label="#{app_name}"
        android:theme="#1{app_theme}"
        >
        <activity android:name="#{packageName}.DefaultActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
'''
    }

    String getSourceSetTxt() {
        return '''
android{
 sourceSets {
        //在main目录中
        main {
            java.srcDirs += "#{cacheDir}/java"
            manifest.srcFile '#{cacheDir}/AndroidManifest.xml'
        }
    }
}
'''
    }

    String getApplicationTxt() {
        return '''
package #{packageName};

import android.app.Application;
import android.util.Log;
/**
 * Created by pqixing on 17-11-24.
 */

public class DefaultAppCation extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DefaultAppCation", "onCreate: app name = "+getPackageName());
        new #1{applicationLike}().onCreate(this);
    }
}'''
    }

    String getActivityTxt() {
        return '''
package #{packageName};

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DefaultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,#1{launchActivity}.class));finish();
    }
}
'''
    }
}
