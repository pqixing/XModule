package com.pqixing.modularization.utils

import java.util.regex.Pattern

class CheckUtils {
    /**
     * 判空处理
     * @param obj
     * @return
     */
    static boolean isEmpty(def obj) {
        if (null == obj || "" == obj.toString() || "null" == obj.toString()) return true
        if (obj instanceof Collection) return obj.isEmpty()
        return false
    }

    static boolean isVersionCode(String str){
        return str?.matches(Pattern.compile("\\d*[.\\d]+"))
    }
}