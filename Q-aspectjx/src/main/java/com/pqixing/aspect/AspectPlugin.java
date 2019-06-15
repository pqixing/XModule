package com.pqixing.aspect;

import com.alibaba.android.arouter.register.launch.PluginLaunch;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.hujiang.gradle.plugin.android.aspectjx.AJXExtension;
import com.hujiang.gradle.plugin.android.aspectjx.AJXTransform;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;

public class AspectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);
        boolean isDebug = project.getGradle().getStartParameter().getTaskNames().toString().toLowerCase().contains("debug");
        //only application module needs this plugin to generate register code
        TransformExtends anExtends = new TransformExtends();
        project.getExtensions().add("register", anExtends);

        AJXExtension ajxExtends = new AJXExtension();
        project.getExtensions().add("aspectjx", ajxExtends);

        DynamicObject dynamicObject = ((DefaultProject) project).getAsDynamicObject();
        DynamicInvokeResult openAspectjx = dynamicObject.tryGetProperty("openAspectjx");
        boolean open = openAspectjx.isFound() && "true".equals(openAspectjx.getValue());

        if (isApp) {
            AppExtension android = project.getExtensions().getByType(AppExtension.class);

            //开始大辰注解切入
            android.registerTransform(new DachenRegisterTransform(anExtends.getFilters()));

            //如果是release模式. 开启Arouter的注解模式
            new PluginLaunch().apply(project);

            //register AspectTransform
            if (open || !isDebug) {
                project.afterEvaluate(p -> {
                    ajxExtends.setEnabled(anExtends.getApectjxApps().contains(project.getName()));
                    if (ajxExtends.getEnabled()) System.out.println("---------------> open Aspect");
                });
                android.registerTransform(new AJXTransform(project));
            }
        }
    }
}
