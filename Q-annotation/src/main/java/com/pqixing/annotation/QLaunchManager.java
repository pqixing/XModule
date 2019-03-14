package com.pqixing.annotation;

import java.util.HashSet;

public class QLaunchManager {
    public static final HashSet<String> activitys = new HashSet<>();
    public static final HashSet<String> likes = new HashSet<>();
    static {
        loadInvokeClass();
    }

    private static void loadInvokeClass() {

    }
}
