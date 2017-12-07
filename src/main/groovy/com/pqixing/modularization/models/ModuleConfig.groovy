package com.pqixing.modularization.models

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class ModuleConfig extends BaseExtension {
    protected final Project project;
    protected final NamedDomainObjectContainer<RunType> runTypes;
    protected final NamedDomainObjectContainer<MavenType> mavenTypes;
    protected final BuildConfig buildConfig
    protected final AndroidConfig androidConfig

    LinkedList<String> reposPaths
    LinkedList<String> defaultImplRepo

    String selectRunType
    String selectMavenType

    ModuleConfig(Project project
                 , NamedDomainObjectContainer<RunType> runTypes
                 , NamedDomainObjectContainer<MavenType> mavenTypes) {
        this.project = project
        this.mavenTypes = mavenTypes
        this.runTypes = runTypes
        buildConfig = new BuildConfig()
        androidConfig = new AndroidConfig()
        reposPaths = new LinkedList<>()
        if (project.hasProperty("reposPaths")) {
            reposPaths += project.ext.get("reposPaths")
        }
        defaultImplRepo = []

        androidConfig.updateMeta(project)
        buildConfig.updateMeta(project)

//        mavenTypes.add(new MavenType("release"))
//        mavenTypes.add(new MavenType("release"))
    }


    void runTypes(String selectItem = "", Closure closure) {
        this.selectRunType = selectItem
        runTypes.configure(closure)
    }

    void mavenTypes(String selectItem = "", Closure closure) {
        this.selectMavenType = selectItem
        mavenTypes.configure(closure)
    }

    void buildConfig(Closure closure) {
        buildConfig.configure(closure)
    }

    void androidConfig(Closure closure) {
        androidConfig.configure(closure)
    }


}
