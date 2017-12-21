package com.pqixing.modularization.plugins

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

    }
}
