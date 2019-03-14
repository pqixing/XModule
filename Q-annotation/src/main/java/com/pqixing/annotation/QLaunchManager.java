package com.pqixing.annotation;

import java.util.HashSet;

public class QLaunchManager {
    private QLaunchManager(){
        loadInvokeClass();
    }
    public static final QLaunchManager getInstance(){
        return new QLaunchManager();
    }
    public  final HashSet<String> activitys = new HashSet<>();
    public final HashSet<String> likes = new HashSet<>();


    private void loadInvokeClass() {

    }
}
