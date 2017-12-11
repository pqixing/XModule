package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class BuildConfig extends BaseExtension {
    String cacheDir
    String outDir
    String groupName = "com.dachen"
    final String projectName
    String packageName

    BuildConfig(Project project) {
        projectName = project.name
        outDir = FileUtils.appendUrls(project.buildDir.path, "outputs", "modularization")
        cacheDir = FileUtils.appendUrls(project.buildDir.path, "temp", "${projectName.hashCode()}", projectName)
        groupName = Default.groupName
        packageName = groupName + '.' + projectName

        project.ext.buildConfig = this

        updateMeta(project)
    }

    @Override
    LinkedList<String> generatorFiles() {
        return ""
    }
}
