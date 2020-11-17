package com.pqixing.modularization.utils

import com.pqixing.model.Module
import com.pqixing.modularization.base.BaseTask

object AndroidUtils {
    fun buildDev(module: Module,taskNames: List<String>):Boolean{
        return module.type == Module.TYPE_LIB && BaseTask.matchTask(listOf(":${module.name}:BuildPxApk",":${module.name}:assembleDev"),taskNames)
    }
}