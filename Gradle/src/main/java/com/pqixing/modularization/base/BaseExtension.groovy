package com.pqixing.modularization.base


import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseExtension {
    public Project project
    BaseExtension(Project project) {
        setProject(project)
    }

/**
 * 配置解析
 * @param closure
 */
    void configure(Closure closure) {
        closure.delegate = this
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure(this)
    }
}
