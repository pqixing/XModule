package com.pqixing.modularization.utils

import com.pqixing.modularization.Default

/**
 * Created by pqixing on 17-12-8.
 */

class NormalUtils {

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

    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String appendUrls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append(File.separator)
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    /**
     * 替换字符串中的变量
     * @param source
     * @return
     */
    static String parseString(String source, Map<String, Object> properties) {
        properties = properties.findAll { map -> !map.key.endsWith("Txt") }
        def builder = new StringBuilder()
        source.eachLine { str ->
            if (NormalUtils.isEmpty(str)) return

            boolean ignore = false
            Default.keyPattern.matcher(str).findAll()?.each { key ->
                if (ignore) return

                def value = properties.find { it.key == (findRealKey(key)) }?.value

                ignore = isEmpty(value) && key.startsWith("#1")

                if (!ignore) str = str.replace(key, String.valueOf(value))
            }
            if (!ignore) builder.append(str).append("\n")//替换#（任意）key
        }
        return builder.toString()
    }
    /**
     * 查找出待替换的key
     * @param source
     * @return
     */
    static String findRealKey(String key) {
        return key.substring(key.indexOf("{") + 1, key.lastIndexOf("}"))
    }
}
