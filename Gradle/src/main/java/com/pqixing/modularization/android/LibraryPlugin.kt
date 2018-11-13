package com.pqixing.modularization.android

import com.pqixing.modularization.Keys
import org.gradle.api.Task

/**
 * Created by pqixing on 17-12-7.
 */

open class LibraryPlugin : AndroidPlugin() {
    val runType = Regex(".*?assemble.*?Dev")
    var forRun = false

    override val applyFiles: List<String> = if (forRun) listOf("com.module.library", Keys.NAME_LIBRARY_RUN) else listOf("com.module.library")

    override val androidPlugin: String
        get() {
            kotlin.run outer@{
                project.gradle.startParameter.taskNames.forEach {
                    if (it.matches(runType)) {
                        forRun = true
                        return@outer
                    }
                }
            }
            return if (forRun) Keys.NAME_APP else Keys.NAME_LIBRARY
        }

    override val ignoreFields: Set<String> = setOf("scr/dev")

    override fun linkTask(): List<Class<out Task>> = listOf()
}
