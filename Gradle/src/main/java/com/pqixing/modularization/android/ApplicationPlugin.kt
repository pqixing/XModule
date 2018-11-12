package com.pqixing.modularization.android


import com.pqixing.modularization.Keys
import org.gradle.api.Task

/**
 * Created by pqixing on 17-12-7.
 */

open class ApplicationPlugin : AndroidPlugin() {
    override val applyFiles: List<String>
        get() = listOf("com.module.application","module.application")
    override val androidPlugin: String = Keys.NAME_APP

    override val ignoreFields: Set<String> = setOf()

    override fun linkTask(): List<Class<out Task>> = listOf()
}
