package com.pqixing.modularization.android.dps

import groovy.lang.Closure

open class DpsExtendsCompat() {

    /**
     * 上传到Maven的版本
     */
    var toMavenVersion = ""

    fun compile(moduleName: String) = compile(moduleName, null)
    fun compile(moduleName: String, closure: Closure<Any?>? = null) {
    }


    fun devCompile(moduleName: String) = devCompile(moduleName, null)
    fun devCompile(moduleName: String, closure: Closure<Any?>? = null) {
    }
}
