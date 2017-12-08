package com.pqixing.modularization.models

import org.gradle.api.Project;

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseContainerExtension extends BaseExtension {
    String name

    Project project

    BaseContainerExtension(String name) {
        this.name = name
    }

    void onCreate(Project project) {
        this.project = project
    }
}
