package com.pqixing.tools

import java.io.File
import java.util.*

/**
 * 多个组件可以通过此操作，传递参数
 */
object PropertiesUtils {

    fun writeGlobe(key: String, value: String) {

    }

    fun readGlobe(key: String, value: String) {

    }

    fun writeProject(key: String, value: String) {

    }

    fun readProject(key: String, value: String) {

    }

    fun readProperties(file: File): Properties {
        val properties = Properties()
        if (file.exists())
            properties.load(file.inputStream())
        return properties
    }

    fun writeProperties(file: File, properties: Properties) {
        if (!file.parentFile.exists()) file.mkdirs()
        properties.store(file.outputStream(), "UTF-8")
    }
}
