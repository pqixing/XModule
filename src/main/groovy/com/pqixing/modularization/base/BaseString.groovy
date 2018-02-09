package com.pqixing.modularization.base

import com.pqixing.modularization.utils.CheckUtils

import java.util.regex.Pattern
/**
 * Created by pqixing on 18-1-31.
 * 基础字符串类，用于格式化变量
 */
class BaseString {
    static final keyPattern = Pattern.compile("#\\d?\\{(?s).*?}")

    public Map<String, Object> params = [:]

    /**
     * 替换字符串中的变量
     * @param source
     * @return
     */
    String get(final String source) {
        if(CheckUtils.isEmpty(params)) return source
        def builder = new StringBuilder()
        source.eachLine { str ->
            if (CheckUtils.isEmpty(str)) return
            boolean ignore = false
            keyPattern.matcher(str).findAll()?.each { key ->
                if (ignore) return

                def value = params.find { it.key == (findRealKey(key)) }?.value

                ignore = CheckUtils.isEmpty(value) && key.startsWith("#1")

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
