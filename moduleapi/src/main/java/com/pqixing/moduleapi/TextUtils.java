package com.pqixing.moduleapi;

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

    static String getStringFields(String fullName, String keyName) {
        if (empty(fullName) || empty(keyName)) return null;
        try {
            return Class.forName(fullName).getDeclaredField(keyName).get(null).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean empty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
