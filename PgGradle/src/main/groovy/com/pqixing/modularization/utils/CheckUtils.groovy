package com.pqixing.modularization.utils

import java.util.regex.Pattern

class CheckUtils {
    /**
     * 判空处理
     * @param obj
     * @return
     */
    static boolean isEmpty(Object obj) {
        if(null == obj) return true
        if(obj instanceof String) return "" == obj.trim().toString() || "null" == obj.trim().toString()
        if (obj instanceof Collection) return obj.isEmpty()
        if (obj instanceof Map) return obj.isEmpty()
        return false
    }

    static boolean isVersionCode(String str) {
        return str?.matches(Pattern.compile("\\d*[.\\d]+"))
    }
    /**
     * 当前目录是不是Gradle Project
     * @param path
     * @return
     */
    static boolean isGradleProject(String path) {
        File f = new File(path, "build.gradle")
        return new File(path, "build.gradle").exists() && !new File(path, "settings.gradle").exists()
    }
    /**
     * 判断当前目录是否是git工程
     * @param path
     * @return
     */
    static boolean isGit(String path) {
        File f = new File(path)
        while (f != null) {
            if (new File(f, ",git").exists()) return true
            f = f.parentFile
        }
        return false
    }
}