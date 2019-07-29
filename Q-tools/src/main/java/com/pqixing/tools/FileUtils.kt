package com.pqixing.tools

import com.pqixing.Tools
import java.io.Closeable
import java.io.File
import java.io.FileInputStream

object FileUtils {
    var clazz: Class<*> = Tools::class.java

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

    /**
     *拷贝文件
     */
    @JvmStatic
    fun copy(from: File, to: File): Boolean {
        if (!from.exists()) return false
        delete(to)
        if (from.isDirectory) from.listFiles().forEach { copy(it, File(to, it.name)) }
        else {
            to.parentFile.mkdirs()
            from.inputStream().copyTo(to.outputStream())
        }
        return true

    }

    @JvmStatic
    fun moveDir(from: File, to: File): Boolean {
        if (!from.exists()) return false
        if (from.isDirectory) from.listFiles().forEach { moveDir(it, File(to, it.name)) }
        else {
            to.parentFile.mkdirs()
            from.renameTo(to)
        }
        return true
    }

    fun replaceOrInsert(start: String, end: String, content: String, source: String): String {
        val regex = Regex("$start.*?$end", RegexOption.DOT_MATCHES_ALL)
        val targetTx = "$start\n$content\n$end"
        return if (source.contains(regex)) {
            source.replace(regex, targetTx)
        } else "$source$targetTx"
    }

    fun closeSafe(stream: Closeable) =try{
        stream.close()
    }catch (e:Exception){
        e.printStackTrace()
    }
}