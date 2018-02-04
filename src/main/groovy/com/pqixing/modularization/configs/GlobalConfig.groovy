package com.pqixing.modularization.configs

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.net.Net
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.wrapper.ProjectWrapper

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
     * 当依赖缺失时，是否拦截报错，默认为true
     */
    public static boolean abortDependenLose = true
    /**
     * 预设仓库地址
     */
    public static Map<String, String> preMavenUrl = [
            "release": "http://192.168.3.7:9527/nexus/content/repositories/android",
            "snap"   : "http://192.168.3.7:9527/nexus/content/repositories/androidsnap",
            "test"   : "http://192.168.3.7:9527/nexus/content/repositories/androidtest"]
    /**
     * 默认的内网镜像依赖组
     */
    static String mavenGroupUrl = "http://192.168.3.7:9527/nexus/content/groups/androidgroup/"
    /**
     * 文档仓库管理,可以设置多个，方便同时管理，只能支持git
     */
    static String docGitUrl = "http://pengqixing:pengqixing@192.168.3.200/android/Document.git"
    /**
     * 文档存放目录
     */
    static String docDirName = "docDir"

    private static boolean init = false

    /**
     * 初始化配置
     * @return
     */
    public static void init() {
        if (init) return
        init = true
        ProjectWrapper wrapper = ProjectWrapper.with(BasePlugin.rootProject)
        String remote = wrapper.get(Keys.REMOTE_CONFIG)
        if (!CheckUtils.isEmpty(remote)) {//有远程配置，优先使用
            if (remote.startsWith(Keys.PREFIX_NET)) updateConfig(Net.get(remote, true))
            else updateConfig(FileUtils.read(remote))
        }
        File configFile = new File(wrapper.project.rootDir, Keys.GLOBAL_CONFIG_NAME)
        if (configFile.exists())
            updateConfig(configFile.text)
    }

    private static void updateConfig(String configStr) {
        Properties config = new Properties()
        config.load(new DataInputStream(configStr.getBytes()))
        GlobalConfig.properties.each { p ->
            updateKey(p.key, config)
        }
    }

    private static void updateKey(String key, Properties config) {
        if (config.containsKey(key)) GlobalConfig."$key" = new GroovyShell().evaluate(config.getProperty(key))
    }
}
