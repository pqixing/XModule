//package com.pqixing.aspect
//
//import com.android.build.gradle.AppExtension
//import com.android.build.gradle.AppPlugin
//
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//
///**
// * 注册注解生成器
// */
//class RegesterPlugin : Plugin<Project> {
//    //    public static AppExtension android;
//
//    override fun apply(project: Project) {
//        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
//        //only application module needs this plugin to generate register code
//        val anExtends = TransformExtends()
//        project.extensions.add("Register", anExtends)
//        if (!isApp) return
//        val android = project.extensions.getByType(AppExtension::class.java)
//        android.registerTransform(RegisterTransform(anExtends.f))
//    }
//}
