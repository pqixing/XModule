package com.pqixing.modularization.plugins

import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.models.RunType
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BasePlugin implements Plugin<Project> {
    abstract String pluginType()

    @Override
    void apply(Project project) {
        ModuleConfig moduleConfig = new ModuleConfig(project
                , project.container(RunType)
                , project.container(MavenType), pluginType())
        project.extensions.add("moduleConfig", moduleConfig)

        project.ext.endConfig = {
            if (it instanceof Closure) it.call(moduleConfig)
            moduleConfig.onConfigEnd()
            moduleConfig.generatorFile()?.findAll {
                !NormalUtils.isEmpty(it)
            }?.all {
                project.apply from: it
            }
        }
    }
}
