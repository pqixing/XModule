package com.pqixing.annotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AnnotationInfo {
    public static final HashSet<String> runActivity = new HashSet<>();
    public static final HashSet<String> runModules = new HashSet<>();
    private static final HashSet<String> routeActivity = new HashSet<>();
    private static final HashSet<String> routeFragment = new HashSet<>();
    private static final HashSet<String> routeServers = new HashSet<>();

    private static final HashSet<String> buildConfigs = new HashSet<>();
    private static final ArrayList<String> configs = new ArrayList<>();

    private static final HashMap<String, Class> path2Class = new HashMap<>();

    public static final Config buildConfig = new Config();


    static {
        loadInvokeClass();
        initClassByNames();
        loadBuildConfig();
    }

    private static void loadBuildConfig() {
        buildConfig.BUILD_TIME = configs.size() > 0 ? Long.parseLong(configs.get(0)) : 0;
        configs.clear();
        for (String s : buildConfigs) {
            Class<?> aClass = forName(s);
            if (aClass == null) continue;
            buildConfig.APPLICATION_ID = getValue("APPLICATION_ID", aClass, null);
            buildConfig.BUILD_TYPE = getValue("BUILD_TYPE", aClass, null);
            buildConfig.FLAVOR = getValue("FLAVOR", aClass, null);
            buildConfig.VERSION_CODE = getValue("VERSION_CODE", aClass, null);
            buildConfig.VERSION_NAME = getValue("VERSION_NAME", aClass, null);
            buildConfig.DEBUG = getValue("DEBUG", aClass, null);
        }
        buildConfigs.clear();
    }

    public static long getBuildTime() {
        return buildConfig.BUILD_TIME;
    }


    private static void initClassByNames() {
        for (String s : routeActivity) {
            Class<?> aClass = forName(s);
            if (aClass == null) continue;
            RouterActivity annotation = aClass.getAnnotation(RouterActivity.class);
            if (annotation == null) continue;
            path2Class.put(annotation.name(), aClass);
        }
        routeActivity.clear();
        for (String s : routeFragment) {
            Class<?> aClass = forName(s);
            if (aClass == null) continue;
            RouterFragment annotation = aClass.getAnnotation(RouterFragment.class);
            if (annotation == null) continue;
            path2Class.put(annotation.name(), aClass);
        }
        routeFragment.clear();
        for (String s : routeServers) {
            Class<?> aClass = forName(s);
            if (aClass == null) continue;
            RouteSevers annotation = aClass.getAnnotation(RouteSevers.class);
            if (annotation == null) continue;
            path2Class.put(annotation.name(), aClass);
        }
        routeServers.clear();
    }

    public static Class forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    private static void loadInvokeClass() {
    }

    public static Class findClassByPath(String path) {
        return path2Class.get(path);
    }

    public static Object findObjectByPath(String path) {
        Class classByPath = findClassByPath(path);
        try {
            return classByPath.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getValue(String fieldName, Class aClass, Object target) {
        try {
            Field field = aClass.getField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getValue(Field field, Object target) {
        try {
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
