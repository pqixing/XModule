package com.pqixing.modularization.base

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin implements Plugin<Project> {
    Project project
    @Override
    void apply(Project project) {
        this.project = project
        addIgnoreFile()
        project.ext.branchName = NormalUtils.getBranchName(project)
        project.ext.lastCommit = NormalUtils.getLastCommit(project)
        project.afterEvaluate {
            createCache()
        }
    }

    void addIgnoreFile() {
        File ignoreFile = project.file(".gitignore")
        if (!ignoreFile.exists()) ignoreFile.createNewFile()
        StringBuilder sb = new StringBuilder(ignoreFile.text)
       Set<String> defSets = ["build","*.iml","cache/"]+ignoreFields
        defSets.each { if (!sb.contains(it)) sb.append("\n$it\n") }
        ignoreFile.write(sb.toString())
    }

    abstract Set<String> getIgnoreFields()

    void createCache() {
        project.task("cleanCache") {
            group = Default.taskGroup
            doLast {
                try {
                    project.clean.execute()
                } catch (Exception e) {
                }
                new File(project.projectDir, ".modularization").deleteDir()
                new File(project.rootDir, ".modularization").deleteDir()
            }
        }
    }
}
