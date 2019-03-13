package com.pqixing.modularization;

import java.lang.reflect.Method;
import java.util.HashMap;

public class JGroovyHelper {
    private static Object TAG = new Object();
    private static HashMap<String, Object> impls = new HashMap<>();


    public static <T> T getImpl(Class<T> tClass) {
        Object o = impls.get(tClass.getSimpleName());
        if (o == null) {
            o = initImpl(tClass);
            impls.put(tClass.getSimpleName(), o);
        }
        return o == TAG ? null : (T) o;
    }

    private static <T> Object initImpl(Class<T> tClass) {
        try {
            Method[] methods = Class.forName("com.pqixing.modularization.GroovyImpl").getMethods();
            for (Method m : methods) {
                if ("getImpl".equals(m.getName())) {
                    return m.invoke(null, tClass);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
