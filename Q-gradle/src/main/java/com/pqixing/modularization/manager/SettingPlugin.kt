package com.pqixing.modularization.manager

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * 设置页面插件
 */
class SettingPlugin : Plugin<Settings> {
    override fun apply(setting: Settings) {
        println("enter setting plugin_-----${setting.rootDir}")
        setting.apply {  }
    }
}