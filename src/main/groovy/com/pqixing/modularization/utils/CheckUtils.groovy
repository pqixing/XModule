package com.pqixing.modularization.utils

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
}