package com.pqixing.annotation;

import java.util.ArrayList;
import java.util.HashSet;

public class QLaunchManager {
    public static final HashSet<String> activitys = new HashSet<>();
    public static final HashSet<String> likes = new HashSet<>();
    public static final ArrayList<String> infos = new ArrayList<>();

    static {
        loadInvokeClass();
    }

    public static String getBuildTimeStr() {
        return infos.size() > 0 ? infos.get(0) : "";
    }

    public static String getBuildConfig() {
        return infos.size() > 1 ? infos.get(1) : "";
    }

    private static void loadInvokeClass() {
    }

}
