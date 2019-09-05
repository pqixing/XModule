package com.android.inputmethod.pinyin;

import java.lang.reflect.Method;

public class SystemPropertiesUtils {
    public static String get(String key) {
        String result = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            result = (String) get.invoke(c, key);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}