package com.pqixing.moduleapi;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class Module {
    public static HashMap<String, IApplicationLike> likeHashMap = new HashMap<>();
    public static final String ENTER_CLASS = "auto.com.pqixing.configs.Enter";
    public static boolean isLoadAppLike = false;

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
    public static final synchronized List<IApplicationLike> installAppLilke(Application app) {
        Set<String> appLikes = new HashSet<>();
        loadAppLike(appLikes, TextUtils.getStringFields(ENTER_CLASS, "CONFIG"),new HashSet<String>());

        List<IApplicationLike> likes = new ArrayList<>();
        if (appLikes.isEmpty()) return likes;

        final Handler uiHandler = new Handler(Looper.getMainLooper());
        final HandlerThread appinit = new HandlerThread("appinit");
        appinit.start();
        Handler handler = new Handler(appinit.getLooper());

        for (String likeName : appLikes) {
            IApplicationLike like = initLike(uiHandler,handler, app, likeName);
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
        isLoadAppLike = true;
        return likes;
    }

    public static IApplicationLike initLike(Handler uiHandle,Handler threadHandle, final Application app, String likeName) {
        IApplicationLike like = null;
        try {
            like = (IApplicationLike) Class.forName(likeName).getConstructor().newInstance();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        if (like != null) {
            final IApplicationLike temp = like;
            uiHandle.post(new Runnable() {
                @Override
                public void run() {
                    temp.init(app);
                    temp.onCreateOnUI(app);
                }
            });
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
    public static final void loadAppLike(Set<String> applikeSets, String configClass,Set<String> hadLoadClass) {

        if(configClass == null ||hadLoadClass.contains(configClass)) return;
        hadLoadClass.add(configClass);

        String launchClass = TextUtils.getStringFields(configClass, "LAUNCH_CONFIG");

        String appLikes = TextUtils.getStringFields(launchClass, "LAUNCH_APPLIKE");
        if (!TextUtils.empty(appLikes)) for (String like : appLikes.split(",")) {
            if (TextUtils.empty(like)) continue;
            applikeSets.add(like);
        }
        String childConfigClass = TextUtils.getStringFields(configClass, "DP_CONFIGS_NAMES");
        if (!TextUtils.empty(childConfigClass)) for (String child : childConfigClass.split(",")) {
            if (TextUtils.empty(child)) continue;
            loadAppLike(applikeSets, child,hadLoadClass);
        }
    }
}
