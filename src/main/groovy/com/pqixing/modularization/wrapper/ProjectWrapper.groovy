package com.pqixing.modularization.wrapper

import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.Print
import org.gradle.api.Project

/**
 * 拓展类Utils
 */
class ProjectWrapper {
    public Project project

    ProjectWrapper(Project project) {
        this.project = project
    }

    static ProjectWrapper with(Project project) {
        return new ProjectWrapper(project)
    }

    public <T> T getExtends(Class<T> tClass) {
        try {
            return project."$tClass.name"
        } catch (Exception e) {
        }
        return null
    }

    public def get(String key) {
        try {
            return project.ext.get(key)
        } catch (Exception e) {
        }
        return null
    }

    public void apply(Map<String, ?> map) {
        try {
            project.apply(map)
        } catch (Exception e) {
            Print.lne("ProjectWrapper apply",e)
        }
    }

    boolean isMaster() {
        return getExtends(GitConfig.class).branchName == "master"
    }

    String getArtifactId() {
        return master ? project.name : "$project.name-b-${getExtends(GitConfig.class).branchName}"
    }
}