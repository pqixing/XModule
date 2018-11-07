package com.pqixing.tools

import java.io.File

object FileUtils {
    lateinit var baseDir: String
    lateinit var cacheDir: String
    fun init(baseDir: String) {
        this.baseDir = cacheDir
        this.cacheDir = "$baseDir/.files"
    }

    @JvmStatic
    fun getTextFromResource(name: String): String {
        val reader = this.javaClass.getResourceAsStream("/$name").bufferedReader()
        val text = reader.readText()
        reader.close()
        return text
    }

    /**
     * @param checkChange 检查是否有变化，如果没有变化，则不写入
     */
    @JvmStatic
    fun writeText(file: File, text: String, checkChange: Boolean = false): String {
        if (checkChange && file.readText() == text) return file.path
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        with(file.writer()) {
            write(text)
            flush()
            close()
        }
        return file.path
    }

    @JvmStatic
    fun delete(f: File): Boolean {
        if (!f.exists()) return false
        if (f.isDirectory) f.listFiles().forEach { delete(it) }
        f.deleteOnExit()
        return true
    }
}