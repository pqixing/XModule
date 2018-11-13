package com.pqixing.tools

import com.pqixing.Tools
import java.io.File

object FileUtils {
    lateinit var clazz: Class<*>
    fun init(clazz: Class<*>) {
        this.clazz = clazz
    }


    @JvmStatic
    fun getTextFromResource(name: String): String {
        val reader = clazz.getResourceAsStream(name).reader()
        val text = reader.readText()
        reader.close()
        return text
    }

    /**
     * @param checkChange 检查是否有变化，如果没有变化，则不写入
     */
    @JvmStatic
    fun writeText(file: File, text: String, checkChange: Boolean = false): String {
        if (checkChange && readText(file) == text) return file.path
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        with(file.writer()) {
            write(text)
            flush()
            close()
        }
        return file.path
    }

    @JvmStatic
    fun readText(f: File): String? {
        if (!f.exists()) return null
        return f.readText()
    }

    @JvmStatic
    fun delete(f: File): Boolean {
        if (!f.exists()) return false
        if (f.isDirectory) f.listFiles().forEach { delete(it) }
        if (f.exists()) f.delete()
        return true
    }
}