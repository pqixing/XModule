package com.pqixing.modularization.base

import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseExtension {
    protected Project project
    BaseExtension(Project project){
        this.project = project
        project.ext."${getClass().name}" = this//初始化赋值
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

    abstract LinkedList<String> generatorFiles()

}
