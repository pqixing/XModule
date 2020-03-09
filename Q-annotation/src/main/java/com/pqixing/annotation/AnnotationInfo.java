package com.pqixing.annotation;

import java.util.HashMap;
import java.util.HashSet;

public class AnnotationInfo {
    private static final HashSet<String> runActivity = new HashSet<>();
    private static final HashSet<String> runModules = new HashSet<>();
    private static final HashSet<String> routeActivity = new HashSet<>();
    private static final HashSet<String> routeFragment = new HashSet<>();
    private static final HashSet<String> routeServers = new HashSet<>();

    private static final HashSet<String> buildConfigs = new HashSet<>();

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
            try {
                Class<?> aClass = Class.forName(s);
                RouterActivity annotation = aClass.getAnnotation(RouterActivity.class);
                path2Class.put(annotation.name(), aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        routeActivity.clear();
        for (String s : routeFragment) {
            try {
                Class<?> aClass = Class.forName(s);
                RouterFragment annotation = aClass.getAnnotation(RouterFragment.class);
                path2Class.put(annotation.name(), aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        routeFragment.clear();
        for (String s : routeServers) {
            try {
                Class<?> aClass = Class.forName(s);
                RouteSevers annotation = aClass.getAnnotation(RouteSevers.class);
                path2Class.put(annotation.name(), aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        routeServers.clear();
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
