package com.pqixing.intellij.utils

import com.intellij.openapi.project.Project
import com.pqixing.tools.FileUtils
import java.io.File
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object DachenHelper {
    /**
     * 2019-03-06 17:00
     */
    fun loadApksForNet(): LinkedHashMap<String, String> {
        val baseUrl = "https://dev.downloads.mediportal.com.cn:9000/"
        val updateTag = "<div class=\"desc\">"
        val nameTag = "<span class=\"name\">"
        val downloadTag = "downloadUrl=\""
        val conn = URL(baseUrl + "android").openConnection()
        (conn as HttpsURLConnection).setHostnameVerifier { s, sslSession -> true }

        var name = ""
        var updateTime = ""

        val result = LinkedHashMap<String, String>()
        conn.inputStream.bufferedReader().readLines().forEach { l ->
            val ui = l.indexOf(updateTag)
            if (ui > 0) updateTime = l.substring(l.indexOf(":", ui) + 1, l.lastIndexOf("</div>"))
            val ni = l.indexOf(nameTag)
            if (ni > 0) name = l.substring(ni + nameTag.length, l.lastIndexOf("</span>"))
            val di = l.indexOf(downloadTag)
            if (di > 0) {
                val si = di + downloadTag.length + 1
                result.put("$updateTime     $name", baseUrl + l.substring(si, l.indexOf("\"", si)))
            }
        }
        return result

    }

    /**
     *https://192.168.3.211:9000/apk/Medical-yhq_1901.apk
     * 下载apk文件
     */
    fun downloadApk(project: Project, name: String, url: String): String {
        val file = File(project.basePath, "build/apks/${name.replace(Regex(" |-|_|:"), "")}.apk")
        try {
            file.parentFile.mkdirs()
            FileUtils.delete(file)
            val lastS = url.lastIndexOf("/")
            val lastD = url.lastIndexOf(".")
            val newUrl = url.substring(0, lastS + 1) + URLEncoder.encode(url.substring(lastS + 1, lastD), "utf-8") + url.substring(lastD)
            val connection = URL(newUrl).openConnection()
            (connection as HttpsURLConnection).setHostnameVerifier { s, sslSession -> true }
            val stream = file.outputStream()
            connection.getInputStream().copyTo(stream)
            connection.inputStream.close()
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            return ""
        }
        return file.absolutePath
    }
}