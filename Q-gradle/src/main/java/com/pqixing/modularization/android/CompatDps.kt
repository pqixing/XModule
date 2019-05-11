package com.pqixing.modularization.android

import com.pqixing.Tools
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.base.BaseExtension
import groovy.lang.Closure

import org.gradle.api.Project

/**
 * 依赖兼容
 */
class CompatDps(project: Project, val dpsExtends: DpsExtends) : BaseExtension(project) {

    /**
     * 桥接到新的依赖管理
     */
    fun dependModules(closure: Closure<Any?>) {
        dpsExtends.configure(closure)
        Tools.println("--------------moduleConfig { dependModules{} } is deprecated , use innerDps{ } instead!!!")
    }

    /**
     * 兼容旧的设置，但是其实已经无用了
     */
    fun androidConfig(closure: Closure<out Any?>) {
        Tools.println("--------------androidConfig{} is deprecated and do nothing,you can remove it")
    }

    fun mavenTypes(closure: Closure<out Any?>) {
        Tools.println("--------------mavenTypes{} is deprecated and do nothing,you can remove it")
    }

    fun toMavenVersion(version:String){
        dpsExtends.toMavenVersion = version
    }

    fun toMavenDesc(desc:String){
        dpsExtends.toMavenDesc = desc
    }
}
