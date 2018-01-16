package com.pqixing.modularization.base

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-20.
 */

class BasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.ext.branchName = NormalUtils.getBranchName(project)
        project.ext.lastCommit = NormalUtils.getLastCommit(project)
        project.afterEvaluate {
            createCache(project)
        }
    }

    void createCache(Project project){
        project.task("cleanCache") {
            group = Default.taskGroup
            doLast {
                try {
                    project.clean.execute()
                }catch (Exception e){}
                new File(project.projectDir,".modularization").deleteDir()
                new File(project.rootDir, ".modularization").deleteDir()
            }
        }
    }
}
