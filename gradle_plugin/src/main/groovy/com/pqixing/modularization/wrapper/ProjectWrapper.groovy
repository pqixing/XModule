package com.pqixing.modularization.wrapper

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.TextUtils
import org.gradle.api.Project
import org.gradle.api.Task
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

    public <T extends Task> T getTask(Class<T> tClass) {
        return project."${BaseTask.getTaskName(tClass)}"
    }

    public def get(String key) {
        try {
            return project.ext.get(key)
        } catch (Exception e) {
        }
        return null
    }

    public void apply(Map<String, ?> map) {
        if (CheckUtils.isEmpty(map)) return
        try {
            project.apply(map)
        } catch (Exception e) {
            Print.lne("ProjectWrapper apply $map\n", e)
        }
    }

    boolean isMaster() {
        return getExtends(GitConfig.class).branchName == "master"
    }

    String getArtifactId() {
        return TextUtils.getBranchArtifactId(project.name, this)
    }

    String getPluginName() {
        return get(Keys.NAME_PUGLIN)
    }

    Project findProject(String name) {
        return project.rootProject.allprojects.find { it.name == name }
    }
}