package com.pqixing.tools

import java.io.File
import java.util.*

/**
 * 多个组件可以通过此操作，传递参数
 */
object PropertiesUtils {

    fun readProperties(file: File): Properties {
        val properties = Properties()
        if (file.exists()){
            val stream = file.inputStream()
            properties.load(stream)
            FileUtils.closeSafe(stream)
        }
        return properties
    }

    fun writeProperties(file: File, properties: Properties) {
        if (!file.exists()) file.parentFile.mkdirs()
        val out = file.outputStream()
        properties.store(out, "UTF-8")
        FileUtils.closeSafe(out)
    }
}
