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
    String defRepoPath
    String rootPath

    BuildConfig(Project project) {
        projectName = project.name
        rootPath = project.projectDir.path.replace("\\","\\\\")
        outDir = FileUtils.appendUrls(rootPath, ".modularization", projectName)
        cacheDir = FileUtils.appendUrls(outDir, ".cache")
        defRepoPath = FileUtils.appendUrls(rootPath, ".modularization", ".repoVersions")
        File ignoreFile = new File(rootPath,".gitignore")
        if (!ignoreFile.exists()) ignoreFile.createNewFile()
        if (!ignoreFile.text.contains(".modularization")) ignoreFile.append("\n.modularization \n")

        groupName = Default.groupName
        packageName = groupName + '.' + projectName

        project.ext.buildConfig = this

//        updateMeta(project)
    }

    @Override
    LinkedList<String> generatorFiles() {
        return ""
    }

    @Override
    public String toString() {
        return "BuildConfig{" +
                "cacheDir='" + cacheDir + '\'' +
                ", outDir='" + outDir + '\'' +
                ", groupName='" + groupName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
