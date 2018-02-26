package com.pqixing.moduleapi;

import android.app.Application;

public class LaunchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        for (IApplicationLike like : Module.installAppLilke(this)) {
            like.onVirtualCreate(this);
        }
    }
}
