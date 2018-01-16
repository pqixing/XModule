package com.pqixing.modularization.plugins

import com.pqixing.modularization.Default
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.models.RunType
import com.pqixing.modularization.tasks.DocSyncTask
import com.pqixing.modularization.tasks.UpdateVersionsTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

abstract class AndroidBasePlugin extends BasePlugin {
    abstract String pluginType()

    @Override
    void apply(Project project) {
        super.apply(project)
        ModuleConfig moduleConfig = new ModuleConfig(project
                , project.container(RunType)
                , project.container(MavenType), pluginType())
        project.extensions.add("moduleConfig", moduleConfig)
        //允许配置全局使用的配置文件
        if (project.hasProperty("allProjectConfig")) {
            String path = project.ext.get("allProjectConfig")
            if (new File(path).exists()) project.apply from: path
        }
        project.ext.endConfig = {
            applySecondConfig(project)
            if (it instanceof Closure) it.call(moduleConfig)

            createVersionsUpdateTask(project, moduleConfig)

            moduleConfig.onConfigEnd()
            moduleConfig.generatorFiles()?.findAll {
                !NormalUtils.isEmpty(it)
            }?.each {
                project.apply from: it
            }
            project.afterEvaluate {
                moduleConfig.afterApplyAndroid()
                createDependencyTask(project, new File(project.buildConfig.outDir, "dependency.txt"))
            }
            project.task("docSync", type: DocSyncTask) {
                docFileDirs = moduleConfig.docFileDirs
                updateDesc = moduleConfig.mavenTypes.release.updateDesc

                File docFile = project.file("doc-${project.name}.md")
                if (!docFile.exists()) docFile.createNewFile()
            }
        }
        project.ext.fromRepo = { key, value = "" ->
            moduleConfig.getRepoVersionStr(key, value)
        }
        project.ext.support_v7 = moduleConfig.androidConfig.support_v7
        project.ext.support_v4 = moduleConfig.androidConfig.support_v4
        project.ext.printPros = { pro -> Print.lnPro(pro) }


    }

    void createDependencyTask(Project project, File outFile) {
        project.task("dependency", type: org.gradle.api.tasks.diagnostics.DependencyReportTask) {
            group = Default.taskGroup
            outputFile = outFile
            doLast {
                FileUtils.writeDependency(project, outFile)
            }
        }.execute()
    }


    void createVersionsUpdateTask(Project project, ModuleConfig config) {
        config.buildConfig.defRepoPath = FileUtils.appendUrls(project.rootDir.absolutePath, ".modularization", "module_${config.mavenType.name}.version")
        project.task("updateVersions", type: UpdateVersionsTask) {
            outPath = config.buildConfig.defRepoPath
            mavenUrl = config.mavenType.maven_url
            compileGroup = config.buildConfig.groupName
            modules += config.dependModules.moduleNames
        }

        //如果设置自动同步，或者之前没有更新过版本号，则先更新版本号
        if (config.updateBeforeSync || !new File(config.buildConfig.defRepoPath).exists()) project.task("updateVersions1", type: UpdateVersionsTask) {
            group = "other"
            outPath = config.buildConfig.defRepoPath
            mavenUrl = config.mavenType.maven_url
            compileGroup = config.buildConfig.groupName
            modules += config.dependModules.moduleNames
            searchModules = false
        }.execute()
    }

    void applySecondConfig(Project project) {
        File localConfig = project.file("local.gradle")
        if (localConfig.exists()) {
            project.apply from: localConfig.path
        }else {
            localConfig.write('''//local config file  , do not commit
moduleConfig{
        
        
        
        
        
}''')
        }
        File ignoreFile = project.file(".gitignore")
        if (!ignoreFile.exists()) ignoreFile.createNewFile()
        if (!ignoreFile.text.contains("second.gradle")) ignoreFile.append("\nsecond.gradle\n")
        if (!ignoreFile.text.contains("local.gradle")) ignoreFile.append("\nlocal.gradle\n")
        if (!ignoreFile.text.contains("build")) ignoreFile.append("\nbuild/\n")
        if (!ignoreFile.text.contains("${project.name}.iml")) ignoreFile.append("\n${project.name}.iml\n")

        //允许配置全局使用的配置文件
        if (project.hasProperty("hiddenConfig")) {
            String path = project.ext.get("hiddenConfig")
            if (new File(path).exists()) project.apply from: path
        }
    }
}
