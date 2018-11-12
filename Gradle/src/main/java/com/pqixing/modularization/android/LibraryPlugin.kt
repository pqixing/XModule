package com.pqixing.modularization.android

import com.pqixing.modularization.Keys
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by pqixing on 17-12-7.
 */

open class LibraryPlugin : AndroidPlugin() {
    override val applyFiles: List<String>
        get() = listOf("com.module.library","module.library")

    override val androidPlugin: String = Keys.NAME_LIBRARY

    override val ignoreFields: Set<String> = setOf("scr/dev")

    override fun linkTask(): List<Class<out Task>> = listOf()
}
