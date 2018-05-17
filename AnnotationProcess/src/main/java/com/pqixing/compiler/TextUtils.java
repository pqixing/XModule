package com.pqixing.compiler;

public class TextUtils {
    /**
     * 只保留数字和字母
     *
     * @param str
     * @return
     */
    static String numOrLetter(String str) {
        if (str == null) return "";
        return str.trim().replaceAll("[^0-9a-zA-Z]", "");
    }

    static String firstUp(String source) {
        if (source == null) return "";
        return source.substring(0, 1).toUpperCase() + source.substring(1);
    }

    static String className(String source) {
        return firstUp(numOrLetter(source));
    }
}
