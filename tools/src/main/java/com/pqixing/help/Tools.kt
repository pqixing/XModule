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

    fun loadConfig(basePath: String, extras: Map<String, Any?> = emptyMap()): Config {
        val configFile = File(basePath, EnvKeys.USER_CONFIG)
        if (!configFile.exists()) FileUtils.writeText(configFile, FileUtils.fromRes(configFile.name))

        val config = try {
            val parseClass = GroovyClassLoader().parseClass(configFile)
            JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), Config::class.java)
        } catch (e: Exception) {
            Config()
        }
        /**
         * 从系统配置中加载对应的变量
         */
        config.javaClass.fields.forEach {
            val key = it.name
            it.isAccessible = true

            //从ext或者gradle.properties中读取配置信息
            kotlin.runCatching {
                val extValue = extras[key]?.toString()
                if (extValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, extValue.toBoolean())
                    String::class.java -> it.set(config, extValue)
                }
            }

            //从传入的环境中读取配置信息
            kotlin.runCatching {
                val envValue = TextUtils.getSystemEnv(key)
                if (envValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, envValue.toBoolean())
                    String::class.java -> it.set(config, envValue)
                }
            }
        }
        return config
    }

    fun loadManifest(basePath: String?): ManifestModel? = File(basePath, EnvKeys.XML_MANIFEST).takeIf { it.exists() }?.let { XmlHelper.parseManifest(it) }
}

fun String.getEnvValue(): String? = try {
    System.getProperty(this)
} catch (e: Exception) {
    null
}