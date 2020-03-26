package com.pqixing.annotation;

import java.util.HashMap;
import java.util.HashSet;

public class AnnotationInfo {
    public static final HashSet<String> runActivity = new HashSet<>();
    public static final HashSet<String> runModules = new HashSet<>();
    private static final HashSet<String> routeActivity = new HashSet<>();
    private static final HashSet<String> routeFragment = new HashSet<>();
    private static final HashSet<String> routeServers = new HashSet<>();

    public static final HashSet<String> buildConfigs = new HashSet<>();

    private static final HashMap<String, Class> path2Class = new HashMap<>();

    private static final long buildTime = 0;


    static {
        loadInvokeClass();
        initClassByNames();
    }

    public static long getBuildTime() {
        return buildTime;
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


}
