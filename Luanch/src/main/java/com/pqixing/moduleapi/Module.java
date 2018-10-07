package com.pqixing.moduleapi;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.*;

public class Module {
    public static final String TAG = "LuanchApi";
    public static final String SP_CACHE_NAME = "moduleapi";
    public static final String LAST_VERSION_NAME = "LAST_VERSION_NAME";
    public static final String LAST_VERSION_CODE = "LAST_VERSION_CODE";
    public static final String MODULE_SP_KEY_APPLIKE = "module_sp_key_applike";

    private static boolean debug = false;

    public static final void openDebug() {
        debug = true;
    }

    public static HashMap<String, IApplicationLike> likeHashMap = new HashMap<>();
    public static final String ENTER_CLASS = "auto.com.pqixing.configs.Enter";
    public static boolean isLoadAppLike = false;

    /**
     * 安装运行AppLike
     */
    public static final HashMap<String, Set<Class>> installActivity() {
        long start = System.currentTimeMillis();

        HashMap<String, Set<String>> nameByGroup = new HashMap<>();

        loadClassByKey(nameByGroup, TextUtils.getStringFields(ENTER_CLASS, "CONFIG"), new HashSet<String>(), "LAUNCH_ACTIVITY");

        HashMap<String, Set<Class>> classByGroup = new HashMap<>();

        Set<Class> clazz = null;
        for (Map.Entry<String, Set<String>> name : nameByGroup.entrySet()) {
            classByGroup.put(name.getKey(), clazz = new HashSet<>());
            for (String s : name.getValue()) {
                Class aClass = forName(s);
                if (aClass == null) continue;
                clazz.add(aClass);
            }
        }
        Log.i(TAG, "installActivity over: count " + (System.currentTimeMillis() - start));
        return classByGroup;
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
    public static final synchronized List<IApplicationLike> installAppLike(Application app) {
        long start = System.currentTimeMillis();
        Set<String> appLikes = new HashSet<>();
        SharedPreferences sp = app.getSharedPreferences(SP_CACHE_NAME, Context.MODE_PRIVATE);
        if (debug || PackageUtils.isNewVersion(app)) {
            loadClassByKey(appLikes, TextUtils.getStringFields(ENTER_CLASS, "CONFIG"), new HashSet<String>(), "LAUNCH_APPLIKE");
            sp.edit().putStringSet(MODULE_SP_KEY_APPLIKE, appLikes).apply();
            PackageUtils.updateVersion(app);
        } else {
            appLikes = sp.getStringSet(MODULE_SP_KEY_APPLIKE, new HashSet<String>());
        }

        List<IApplicationLike> likes = new ArrayList<>();
        if (appLikes.isEmpty()) return likes;

        final Handler uiHandler = new Handler(Looper.getMainLooper());
        final HandlerThread appinit = new HandlerThread("appinit");
        appinit.start();
        Handler handler = new Handler(appinit.getLooper());

        for (String likeName : appLikes) {
            IApplicationLike like = initLike(uiHandler, handler, app, likeName);
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
        Log.i(TAG, "installAppLike over: count " + (System.currentTimeMillis() - start));
        return likes;
    }

    public static IApplicationLike initLike(Handler uiHandle, Handler threadHandle, final Application app, String likeName) {
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
     * 从配置文件加载出对应的类
     *
     * @param keyClassMap
     * @param configClass
     * @param hadLoadClass
     * @param key
     */
    public static final void loadClassByKey(Map<String, Set<String>> keyClassMap, String configClass, Set<String> hadLoadClass, String key) {
        if (configClass == null || hadLoadClass.contains(configClass)) return;
        hadLoadClass.add(configClass);

        String launchClass = TextUtils.getStringFields(configClass, "LAUNCH_CONFIG");

        String keyField = TextUtils.getStringFields(launchClass, key);
        if (!TextUtils.empty(keyField)) for (String f : keyField.split(",")) {
            if (TextUtils.empty(f)) continue;
            Set<String> keyClass = keyClassMap.get(launchClass);
            if (keyClass == null) {
                keyClass = new HashSet<>();
                keyClassMap.put(launchClass, keyClass);
            }
            keyClass.add(f);
        }
        String childConfigClass = TextUtils.getStringFields(configClass, "DP_CONFIGS_NAMES");
        if (!TextUtils.empty(childConfigClass)) for (String child : childConfigClass.split(",")) {
            if (TextUtils.empty(child)) continue;
            loadClassByKey(keyClassMap, child, hadLoadClass, key);
        }
    }

    /**
     * 从配置文件加载出对应的key
     *
     * @param keyClass
     * @param configClass
     */
    public static final void loadClassByKey(Set<String> keyClass, String configClass, Set<String> hadLoadClass, String key) {
        HashMap<String, Set<String>> map = new HashMap<>();
        loadClassByKey(map, configClass, hadLoadClass, key);

        for (Set<String> s : map.values()) {
            if (s != null) keyClass.addAll(s);
        }
    }
}
