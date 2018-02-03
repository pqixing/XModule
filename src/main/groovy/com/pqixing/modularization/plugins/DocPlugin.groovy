package com.pqixing.modularization.plugins

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-20.
 */

class DocPlugin extends BasePlugin {
    @Override
    void apply(Project project) {
        super.apply(project)
//        println("name Document : ${NormalUtils.getBranchName(project)}  lastCommit : ${NormalUtils.getLastCommit(project)}")

//        def updateDoc = project.task("updateDoc", type: UpdateLog) {
//            compileGroup = Default.groupName
//            envs = ["test": Default.maven_url_test, "release": Default.maven_url_release,"debug":Default.maven_url_debug]
//        }
//        UpdateLog.findModules(project.file("readme")).each {
//            project.task("log-$it", type: UpdateDetail) {
//                compileGroup = Default.groupName
//                envs = ["test": Default.maven_url_test, "release": Default.maven_url_release,"debug":Default.maven_url_debug]
//                moduleName = "router"
//                doLast {
//                    updateDoc.execute()
//                }
//            }
//        }
    }

    @Override
    String getPluginName() {
        return Keys.NAME_APP
    }

    @Override
    Set<String> getIgnoreFields() {
        return ["updatelog"]
    }
}
