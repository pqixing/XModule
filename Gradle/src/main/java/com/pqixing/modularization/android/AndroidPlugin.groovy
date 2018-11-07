package com.pqixing.modularization.android


import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.base.BasePlugin
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class AndroidPlugin extends BasePlugin {

    @Override
    void apply(Project project) {
        super.apply(project)
        //根据情况进行不同的Android插件依赖
        project.apply plugin: "${getAndroidPlugin()}"

        //创建配置读取
        def dpsExt = project.extensions.create(Keys.CONFIG_DPS, DpsExtends.class, project)
        def moduleConfig = new CompatDps(project, dpsExt)
        project.extensions.add(Keys.CONFIG_MODULE, moduleConfig)
        project.ext.endConfig = { if (it instanceof Closure) it.call(moduleConfig) }


        //在工程处理后，处理组件依赖
        project.afterEvaluate {

        }
    }

    protected abstract String getAndroidPlugin()
}
