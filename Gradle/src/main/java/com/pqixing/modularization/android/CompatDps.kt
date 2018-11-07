package com.pqixing.modularization.android

import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.base.BaseExtension
import groovy.lang.Closure

import org.gradle.api.Project

/**
 * 依赖兼容
 */
class CompatDps(project: Project, val dpsExtends: DpsExtends) : BaseExtension(project) {

    val andriodConfig: AndroidConfig = AndroidConfig()
    /**
     * 桥接到新的依赖管理
     */
    fun dependModules(closure: Closure<Any?>) {
        dpsExtends.configure(closure)
    }

    /**
     * 兼容旧的设置，但是其实已经无用了
     */
    fun androidConfig(closure: Closure<out Any?>) {
        closure.delegate = andriodConfig
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call(andriodConfig)
    }
}
