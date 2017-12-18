package com.pqixing.modularization.plugins

import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.models.RunType
import com.pqixing.modularization.tasks.UpdateVersionsTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
            applySecondConfig(project)
            createVersionsUpdateTask(project, moduleConfig)
            if (it instanceof Closure) it.call(moduleConfig)
            moduleConfig.onConfigEnd()
            moduleConfig.generatorFiles()?.findAll {
                !NormalUtils.isEmpty(it)
            }?.each {
                project.apply from: it
            }
            moduleConfig.afterApplyAndroid()
        }
        project.ext.fromRepo = { key, value = "" ->
            moduleConfig.getRepoVersionStr(key, value)
        }
        project.ext.support_v7 = moduleConfig.androidConfig.support_v7
        project.ext.support_v4 = moduleConfig.androidConfig.support_v4

    }

    void createVersionsUpdateTask(Project project, ModuleConfig config) {
        Task t = project.task("updateVersions", type: UpdateVersionsTask) {
            config.buildConfig.defRepoPath = FileUtils.appendUrls(config.buildConfig.rootPath, ".modularization",config.selectMavenType, ".repoVersions")
            outPath = config.buildConfig.defRepoPath
            mavenUrl = config.mavenType.maven_url
            compileGroup = config.buildConfig.groupName
            modules += config.defaultImpl
            modules += config.defaultApk
        }

        if (config.updateBeforeSync) t.execute()
    }

    void applySecondConfig(Project project) {
        File secondConfig = project.file("second.gradle")
        if (secondConfig.exists()) project.apply from: secondConfig.path
        File ignoreFile = project.file(".gitignore")
        if (!ignoreFile.exists()) ignoreFile.createNewFile()
        if (!ignoreFile.text.contains("second.gradle")) ignoreFile.append("\nsecond.gradle\n")
    }
}
