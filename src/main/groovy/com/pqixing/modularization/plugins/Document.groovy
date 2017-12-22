package com.pqixing.modularization.plugins

import com.pqixing.modularization.Default
import com.pqixing.modularization.tasks.UpdateDetail
import com.pqixing.modularization.tasks.UpdateLog
import com.pqixing.modularization.utils.Print;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by pqixing on 17-12-20.
 */

public class Document implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Print.init(project)
        project.task("updateLog", type: UpdateLog) {
            compileGroup = Default.groupName
            envs = ["test": Default.maven_url_test, "release": Default.maven_url_release]
        }
        project.task("update-router", type: UpdateDetail) {
            compileGroup = Default.groupName
            envs = ["test": Default.maven_url_test, "release": Default.maven_url_release]
            moduleName = "router"
        }
    }
}
