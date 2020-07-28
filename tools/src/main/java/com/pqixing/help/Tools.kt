package com.pqixing.help

import com.alibaba.fastjson.JSON
import com.pqixing.Config
import com.pqixing.EnvKeys
import com.pqixing.interfaces.ILog
import com.pqixing.model.ManifestModel
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import java.io.File
import java.util.*
import kotlin.system.exitProcess


object Tools {
    var log = true
    fun println(l: String?) {
        if (log) logger?.println(l) ?: kotlin.io.println(l)

    }

    fun printError(exitCode: Int, l: String?) {
        logger?.printError(exitCode, l) ?: kotlin.run {
            if (log) kotlin.io.println(l)
            exitProcess(exitCode)
        }
    }

    fun println(exitCode: Int, l: String?) = if (exitCode < 0) printError(exitCode, l) else println(l)

    var logger: ILog? = null

    fun getPsw(value: String): String {
        if (value.startsWith("sk:")) return base64Decode(value.substring(3))
        if (value.isNotEmpty()) println("Warming::password suggest $value -> ${"sk:" + base64Encode(value)}")
        return value
    }

    private fun base64Encode(source: String) = String(Base64.getEncoder().encode(source.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)
    private fun base64Decode(source: String) = String(Base64.getDecoder().decode(source.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)

}

fun String.getEnvValue(): String? = try {
    System.getProperty(this)
} catch (e: Exception) {
    null
}