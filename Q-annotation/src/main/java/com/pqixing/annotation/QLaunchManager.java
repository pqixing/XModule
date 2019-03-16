package com.pqixing.annotation;

import java.util.HashSet;

public class QLaunchManager {
    public static final HashSet<String> activitys = new HashSet<>();
    public static final HashSet<String> likes = new HashSet<>();
    public static final String buildTimeStr = "";
    public static final String desc1 = "";
    public static final String desc2 = "";

    static {
        loadInvokeClass();
    }

    private static void loadInvokeClass() {
    }
}
