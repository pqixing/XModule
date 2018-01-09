package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.tasks.UploadTask
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

class ModuleConfig extends BaseExtension {
    protected final Project project

    final NamedDomainObjectContainer<RunType> runTypes
    final NamedDomainObjectContainer<MavenType> mavenTypes
    final BuildConfig buildConfig
    final AndroidConfig androidConfig

    final String pluginType

    Dependencies dependModules
    RepoVersions dependVersions

    String docFileDirs

    /**
     * 是否在同步前，更新一遍版本号
     */
    boolean updateBeforeSync = true
    RunType runType
    MavenType mavenType


    String pom_version
    boolean uploadEnable
    //集成默认的依赖库
    boolean addDefaultImpl = true

    ModuleConfig(Project project
                 , NamedDomainObjectContainer<RunType> runTypes
                 , NamedDomainObjectContainer<MavenType> mavenTypes, String pluginType) {
        this.pluginType = pluginType
        this.project = project
        buildConfig = new BuildConfig(project)

        androidConfig = new AndroidConfig(project)
//        androidConfig.updateMeta(project)

        dependModules = new Dependencies(project)
        dependVersions = new RepoVersions(project)

        this.mavenTypes = mavenTypes
        mavenTypes.whenObjectAdded { it.onCreate(project) }

        mavenTypes.add(new MavenType("release"))
        mavenTypes.add(new MavenType("debug"))
        mavenTypes.add(new MavenType("test"))

        this.runTypes = runTypes
        runTypes.whenObjectAdded { it.onCreate(project) }

        mavenType = mavenTypes.debug
        if(project.hasProperty("addDefaultImpl")) addDefaultImpl = "Y" == project.ext.get("addDefaultImpl")
        if(project.hasProperty("offline")) updateBeforeSync = "Y"!=project.offline
    }

    String getCompilePluginType() {
        if ("application" == pluginType) return pluginType
        if (runType?.asApp) return "application"
        return "library"
    }


    void runTypes(Closure closure) {
        runTypes.configure(closure)
    }


    void mavenTypes(String pom_version = this.pom_version, Closure closure) {
        this.pom_version = pom_version
        mavenTypes.configure(closure)
    }

    void dependModules(Closure closure) {
        dependModules.configure(closure)
    }

    void dependVersions(Closure closure) {
        dependVersions.configure(closure)
    }

    void buildConfig(Closure closure) {
        buildConfig.configure(closure)
    }

    void androidConfig(Closure closure) {
        androidConfig.configure(closure)
    }

    void onConfigEnd() {
        if (new File(buildConfig.defRepoPath).exists()) dependVersions.configPaths.addFirst(buildConfig.defRepoPath)
        dependVersions.endConfig()

        dependModules.baseGroup = buildConfig.groupName + ".android"
        if (addDefaultImpl) Default.defaultImplRepo.each {
            dependModules.addExImpl(it)
        }
        dependModules.endConfig(dependVersions.versions)
    }

    void afterApplyAndroid() {
//        project.afterEvaluate {
            uploadToMavenTask()
            Print.ln(toString())
//        }
    }

    /**
     * 检查是否可以上传
     * @return
     */
    boolean checkUploadAble() {
        if ("library" != compilePluginType) return false
        if (dependModules.hasLocalCompile()) return false
        return true
    }

    /**
     * 上传数据到maven仓库
     */
    private void uploadToMavenTask() {
        if (!checkUploadAble()) return
        def listTask = []
        //添加上传的任务
//        mavenTypes.each { m ->
        MavenType m = mavenType
        switch (m.name) {
            case "release":
            case "test":
            case "debug":
                m.onCreate(project)
                break
        }
        if (NormalUtils.isEmpty(m.pom_version)) m.pom_version = pom_version
        if (NormalUtils.isEmpty(m.uploadEnable)) m.uploadEnable = uploadEnable

        if (m.uploadEnable && ("release" != m.name || Default.uploadKey == m.uploadKey)) {
            String taskName = "${project.name}Upload($m.name)"
            listTask += project.task(taskName, type: UploadTask) { mavenInfo = m }
        }
//        }
        listTask.each { it.dependsOn project.assembleRelease }
    }

    @Override
    LinkedList<String> generatorFiles() {
        LinkedList<String> files = []
        files += androidConfig.generatorFiles()
        files += mavenType.generatorFiles()

        if (!NormalUtils.isEmpty(runType)) files += runType.generatorFiles()
        files += dependModules.generatorFiles()

        return files
    }
}
