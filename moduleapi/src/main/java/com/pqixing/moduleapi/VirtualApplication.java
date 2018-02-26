package com.pqixing.moduleapi;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class VirtualApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "run as virtual app!!!", Toast.LENGTH_LONG).show();
        for (IApplicationLike like : Module.installAppLilke(this)) {
            like.onVirtualCreate(this);
        }
        Log.i("VirtualApplication", "all application like : " + Module.likeHashMap.keySet());
    }
}
