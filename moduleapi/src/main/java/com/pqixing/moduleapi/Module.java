package com.pqixing.moduleapi;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.pqixing.annotation.LaunchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@LaunchActivity(name = "",group = "",intent = "key:'ddd',key:value")
public class Module {
    public static HashMap<String, IApplicationLike> likeHashMap = new HashMap<>();
    public static final String ENTER_CLASS = "auto.com.pqixing.configs.Enter";

    /**
     * 安装运行AppLike
     */
    public static final List<Class> installActivity() {
        String launchConfig = TextUtils.getStringFields(ENTER_CLASS, "LAUNCH");
        String activityStrs = TextUtils.getStringFields(launchConfig, "LAUNCH_ACTIVITY");
        if (TextUtils.empty(activityStrs)) return new ArrayList<>();
        ArrayList<Class> activityList = new ArrayList<>();
        for (String s : activityStrs.split(",")) {
            Class aClass = forName(s);
            if (aClass == null) continue;
            activityList.add(aClass);
        }
        return activityList;
    }

    public static Class forName(String name) {
        if (TextUtils.empty(name)) return null;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }

    }

    /**
     * 安装运行AppLike
     *
     * @param app
     */
    public static final List<IApplicationLike> installAppLilke(Application app) {
        Set<String> applikes = new HashSet<>();
        loadAppLike(applikes, TextUtils.getStringFields(ENTER_CLASS, "CONFIG"));

        List<IApplicationLike> likes = new ArrayList<>();
        if (applikes.isEmpty()) return likes;

        final HandlerThread appinit = new HandlerThread("appinit");
        appinit.start();
        Handler handler = new Handler(appinit.getLooper());

        for (String likeName : applikes) {
            IApplicationLike like = initLike(handler, app, likeName);
            if (like != null) {
                likes.add(like);
                likeHashMap.put(like.getModuleName(), like);
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                appinit.quit();
            }
        });
        return likes;
    }

    public static IApplicationLike initLike(Handler threadHandle, final Application app, String likeName) {
        IApplicationLike like = null;
        try {
            like = (IApplicationLike) Class.forName(likeName).getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (like != null) {
            like.init(app);
            like.onCreateOnUI(app);
            final IApplicationLike temp = like;
            threadHandle.post(new Runnable() {
                @Override
                public void run() {
                    temp.onCreateOnThread(app);
                }
            });

        }
        return like;
    }

    /**
     * 加载所有的ApplicationLike类
     *
     * @param applikeSets
     * @param configClass
     */
    public static final void loadAppLike(Set<String> applikeSets, String configClass) {
        String launchClass = TextUtils.getStringFields(configClass, "LAUNCH_CONFIG");

        String applikes = TextUtils.getStringFields(launchClass, "LAUNCH_APPLIKE");
        if (!TextUtils.empty(applikes)) for (String like : applikes.split(",")) {
            if (TextUtils.empty(like)) continue;
            applikeSets.add(like);
        }
        String childConfigClass = TextUtils.getStringFields(configClass, "DP_CONFIGS_NAMES");
        if (!TextUtils.empty(childConfigClass)) for (String child : childConfigClass.split(",")) {
            if (TextUtils.empty(child)) continue;
            loadAppLike(applikeSets, child);
        }
    }
}
