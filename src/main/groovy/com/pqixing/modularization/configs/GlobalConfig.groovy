package com.pqixing.modularization.configs

import com.pqixing.modularization.base.BasePlugin

/**
 * Created by pqixing on 17-12-7.
 * 全局配置，主要在gradle.propeties中的配置信息
 */
class GlobalConfig {
    /**
     * 是否开启离线模式，如果开启了离线模式，网络请求默认全部都使用本地的。如果本地不存在缓存时，则会抛出异常
     */
    public static boolean offlineMode = false
    /**
     * 是否在同步前，更新一遍版本号,如果为false，间隔一小时更新一次
     */
    public static boolean updateBeforeSync = true
    //集成默认的依赖库
    public static List<String> autoImpl = ["dcnet", "dccommon", "dcuser", "router", "mvpbase"]
    /**
     * 组别名称
     */
    public static String groupName = "com.dachen.android"

    /**
     * 依赖方式改变
     * localFirst,localOnly,mavenOnly,mavenFirst
     */
    public static String dependenModel = "mavenOnly"

    /**
     * 上传所需的用户名
     */
    public static String mavenUser = "admin"
    public static String mavenPassword = "admin123"
    /**
     * 预设仓库地址
     */
    public static Map<String, String> preMavenUrl = ["release": "http://192.168.3.7:9527/nexus/content/repositories/android", "test": "http://192.168.3.7:9527/nexus/content/repositories/androidtest"]
    /**
     * 文档仓库管理,可以设置多个，方便同时管理，只能支持git
     */
    static final Map<String, String> docGits = ["Document", ""]

    static {
        BasePlugin.rootProject
    }
}
