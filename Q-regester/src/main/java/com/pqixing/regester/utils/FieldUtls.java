package com.pqixing.regester.utils;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FieldUtls {

    public static Object getValue(Object obj,Class clazz, String key) {
        Field field = getField(clazz, key);
        if (field != null) {
            try {
                return field.get(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    public static Field getField(Class clazz, String key) {
        Field field = null;

        try {
            field = clazz.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(key);
            } catch (NoSuchFieldException e1) {
            }
        }
        if (field != null) field.setAccessible(true);
        return field;
    }

    public static void setValue(Object obj,Class clazz, String key, Object value) {
        Field field = getField(clazz, key);
        if (field != null) {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {

            }
        }
    }
}
