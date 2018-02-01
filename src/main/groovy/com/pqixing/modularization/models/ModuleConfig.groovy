package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.dependent.RepoVersions
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.maven.ToMavenTask
import com.pqixing.modularization.runtype.RunType
import com.pqixing.modularization.utils.XmlUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

class ModuleConfig extends BaseExtension {
    final NamedDomainObjectContainer<RunType> runTypes
    final NamedDomainObjectContainer<MavenType> mavenTypes
    final AndroidConfig androidConfig
    Dependencies dependModules
    RepoVersions dependVersions
    PreWriteConfig writeConfig

    RunType runType
    MavenType mavenType

    ModuleConfig(Project project,NamedDomainObjectContainer<RunType> runTypes
                 , NamedDomainObjectContainer<MavenType> mavenTypes) {
        super(project)

        androidConfig = new AndroidConfig(project)
        dependModules = new Dependencies(project)
        dependVersions = new RepoVersions(project)
        writeConfig = new PreWriteConfig(project)

        this.mavenTypes = mavenTypes
        mavenTypes.whenObjectAdded { it.onCreate(project) }

        mavenTypes.add(new MavenType("release"))
        mavenTypes.add(new MavenType("test"))
        mavenTypes.add(new MavenType("DEFAULT"))
        mavenTypes.DEFAULT.onCreate(project)
        mavenTypes.DEFAULT.uploadEnable = true

        this.runTypes = runTypes
        runTypes.whenObjectAdded { it.onCreate(project) }
        runTypes.add(new RunType("DEFAULT"))

        mavenType = mavenTypes.test
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


    void mavenTypes(Closure closure) {
        mavenTypes.configure(closure)
    }

    void dependModules(Closure closure) {
        dependModules.configure(closure)
    }

    void writeConfig(Closure closure) {
        writeConfig.configure(closure)
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
        if (addDefaultImpl&&!Default.defaultImplRepo.contains(project.name)) Default.defaultImplRepo.each {
            dependModules.addExImpl(it)
        }else dependModules.addImpl("router")
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

        MavenType defType = mavenTypes.DEFAULT
        if (XmlUtils.isEmpty(m.pom_version)) m.pom_version = defType.pom_version
        if (XmlUtils.isEmpty(m.uploadEnable)) m.uploadEnable = defType.uploadEnable
        if (XmlUtils.isEmpty(m.updateDesc)) m.updateDesc = defType.updateDesc
        if (XmlUtils.isEmpty(m.uploadKey)) m.uploadKey = defType.uploadKey
        if(!m.focusUpload) m.focusUpload = defType.focusUpload

        if (m.uploadEnable && ("release" != m.name || Default.uploadKey == m.uploadKey)) {
            String taskName = "${project.name}Upload"
            listTask += project.task(taskName, type: ToMavenTask) { mavenInfo = m }
        }
//        }
        listTask.each { it.dependsOn project.assembleRelease }
    }

    @Override
    LinkedList<String> getOutFiles() {
        LinkedList<String> files = []
        files += androidConfig.getOutFiles()
        files += mavenType.getOutFiles()
        if (!XmlUtils.isEmpty(runType)) files += runType.getOutFiles()
        files += dependModules.getOutFiles()

        files += writeConfig.getOutFiles()

        return files
    }
}
