package com.pqixing.modularization.base

import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseContainer extends BaseExtension {
    String name

    BaseContainer(String name) {
        super()
        this.name = name
    }

    void onCreate(Project project) {
        setProject(project)
    }
}
