package com.pqixing.regester;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * 注册注解生成器
 */
public class RegesterPlugin implements Plugin<Project> {
//    public static AppExtension android;

    @Override
    public void apply(Project project) {
        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);
        //only application module needs this plugin to generate register code
        TransformExtends anExtends = new TransformExtends();
        project.getExtensions().add("Register", anExtends);
        if (!isApp) return;
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(new RegisterTransform(anExtends.getF()));
    }
}
