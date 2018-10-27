package com.pqixing.tools

import java.io.File

object FileUtils {
    lateinit var baseDir: String
    lateinit var cacheDir: String
    fun init(baseDir: String) {
        this.baseDir = cacheDir
        this.cacheDir = "$baseDir/.files"
    }

    fun getTextFromResource(name: String): String {
        val reader = this.javaClass.getResourceAsStream("/$name").bufferedReader()
        val text = reader.readText()
        reader.close()
        return text
    }

    /**
     * @param checkChange 检查是否有变化，如果没有变化，则不写入
     */
    fun writeText(file: File, text: String): String {
        if (file.readText() == text) return file.path
        file.parentFile.mkdirs()
        with(file.writer()) {
            write(text)
            flush()
            close()
        }
        return file.path
    }
}