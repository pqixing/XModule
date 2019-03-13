package com.pqixing.modularization.base


import org.gradle.api.Project

import groovy.lang.Closure

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseExtension(val project: Project) {


    /**
     * 配置解析
     * @param closure
     */
    fun configure(closure: Closure<*>) {
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call(this)
    }
}
