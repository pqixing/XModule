package com.pqixing.tools

import com.pqixing.Conts
import java.io.File


internal object CheckUtils {
    /**
     * 判空处理
     * @param obj
     * @return
     */
    @JvmStatic
    fun isEmpty(obj: Any?): Boolean = when (obj) {
        is Collection<*> -> obj.isEmpty()
        is Map<*, *> -> obj.isEmpty()
        is String -> obj.isEmpty() || obj.toLowerCase() == Conts.NULL
        else -> obj?.toString()?.trim()?.isEmpty() ?: false
    }
    @JvmStatic
    fun isVersionCode(str: String?): Boolean = str?.matches(Regex("\\d*[.\\d]+")) ?: false

    /**
     * 当前目录是不是Gradle Project
     * @param path
     * @return
     */
    @JvmStatic
    fun isChildGradleProject(path: String): Boolean {
        return File(path, Conts.BUILD_GRADLE).exists() && !File(path, Conts.SETTINGS_GRADLE).exists()
    }
    /**
     * 判断当前目录是否是git工程
     * @param path
     * @return
     */
    @JvmStatic
    fun isGitDir(path: String): Boolean {
        var f: File? = File(path)
        while (f != null) {
            if (File(f, Conts.DIR_GIT).exists()) return true
            f = f.parentFile
        }
        return false
    }
}