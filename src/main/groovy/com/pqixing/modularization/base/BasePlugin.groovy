package com.pqixing.modularization.base

import com.pqixing.modularization.Keys
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.git.GitPullTask
import com.pqixing.modularization.tasks.CleanCacheTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.wrapper.ProjectWrapper
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin implements Plugin<Project> {
    public static Project rootProject
    Project project
    ProjectWrapper wrapper
    @Override
    void apply(Project project) {
        this.rootProject = project.rootProject
        this.project = project
        wrapper = ProjectWrapper.with(project)
        GlobalConfig.init()
        addIgnoreFile()

        new GitConfig(project)//生成git相关信息
        new BuildConfig(project)//生成一个Build配置信息

        BaseTask.task(project,GitPullTask.class)
        BaseTask.task(project,CleanCacheTask.class)
    }

    void addIgnoreFile() {
        File ignoreFile = project.file(Keys.GIT_IGNORE)
        StringBuilder sb = new StringBuilder(FileUtils.read(ignoreFile))
        Set<String> defSets = ["build", Keys.GLOBAL_CONFIG_NAME,Keys.FOCUS_GRADLE, BuildConfig.dirName, "*.iml"] + ignoreFields

        defSets.each { if (!sb.contains(it)) sb.append("\n$it\n") }
        FileUtils.write(ignoreFile,sb.toString())
    }

    abstract Set<String> getIgnoreFields()
}
