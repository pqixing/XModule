package com.pqixing.modularization.impl

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.pqixing.Tools
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.jetbrains.annotations.NotNull

public class GExtHelper implements IGExtHelper {

    public Object getExtValue(Project project, String key) {
        try {
            return project.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Project project, String key, Object value) {
        try {
            project.ext."$key" = value
        } catch (Exception e) {
        }
    }

    public Object getExtValue(Gradle gradle, String key) {
        try {
            return gradle.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Gradle gradle, String key, Object value) {
        try {
            gradle.ext."$key" = value
        } catch (Exception e) {
        }
    }

    @Override
    void setExtMethod(Project project, String method, Action action) {
        project.ext."$method" = { action?.execute(it) }
    }

    @Override
    void addRepositories(Project project, @NotNull List<String> dependMaven) {
        project.repositories {
            dependMaven.each { l -> if(l.startsWith("http")) maven { url l } else maven { url project.uri(l) }  }
        }
    }

    @Override
    void addSourceDir(Project project, String dir) {
        project.android.sourceSets.main.java.srcDirs += dir
    }

    @Override
    void setApiSourceDir(Project project, String dir, String manifestPath) {
        project.android.sourceSets.main.java.srcDirs = [dir]
        project.android.sourceSets.main.manifest.srcFile manifestPath
        project.android.sourceSets.main.res.srcDirs = ["src/main/resApi"]
        project.android.sourceSets.main.resources.srcDirs = ['src/main/resourcesApi']
        project.android.sourceSets.main.jniLibs.srcDirs = ["src/main/jniLibsApi"]
        project.android.sourceSets.main.assets.srcDirs = ["src/main/assetsApi"]
    }

    @Override
    void setMavenInfo(Project project,String groupId, String artifactId, String version, String name) {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository

        repository.authentication.password = Tools.INSTANCE.getPsw(repository.authentication.password)
        pom.groupId = groupId
        pom.artifactId = artifactId
        pom.version = version
        pom.name = name
    }

    @Override
    Map<String, File> getAndroidOut(Project project, String type) {
        HashMap<String, File> variants = new HashMap<>()
        Object android = project.extensions.getByName("android")
        (android instanceof AppExtension
                ? ((AppExtension) android).applicationVariants
                : ((LibraryExtension) android).libraryVariants)
                .all { BaseVariant app ->
                    variants[app.buildType.name] = app.outputs.last().outputFile
                }
        return variants
    }

    @Override
    void setApplicationId(@NotNull Project project, @NotNull String s) {
        project.android.defaultConfig.applicationId = s
    }
}
