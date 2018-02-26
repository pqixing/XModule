package com.pqixing.modularization

import com.pqixing.modularization.android.AndroidConfig
import com.pqixing.modularization.android.PreWriteConfig
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.runtype.RunType
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
    PreWriteConfig writeConfig

    RunType runType
    MavenType mavenType

    ModuleConfig(Project project, NamedDomainObjectContainer<RunType> runTypes
                 , NamedDomainObjectContainer<MavenType> mavenTypes) {
        super(project)
        androidConfig = new AndroidConfig(project)
        dependModules = new Dependencies(project)
        writeConfig = new PreWriteConfig(project)

        this.mavenTypes = mavenTypes
        mavenTypes.whenObjectAdded { it.onCreate(project) }
        mavenTypes.add(new MavenType("release"))
        mavenTypes.add(new MavenType("test"))
        mavenTypes.add(new MavenType("snap"))
        mavenTypes.add(new MavenType(Keys.DEFAULT))
        mavenType = mavenTypes.test

        this.runTypes = runTypes
        runTypes.whenObjectAdded { it.onCreate(project) }
        runTypes.add(new RunType(Keys.DEFAULT))
        runTypes.add(new RunType(Keys.TEST))


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

    void androidConfig(Closure closure) {
        androidConfig.configure(closure)
    }

    @Override
    LinkedList<String> getOutFiles() {
        LinkedList<String> files = []
        files += androidConfig.outFiles
        files += mavenType?.outFiles
        files += runType?.outFiles
        files += dependModules.outFiles
        files += writeConfig?.outFiles
        return files
    }
}
