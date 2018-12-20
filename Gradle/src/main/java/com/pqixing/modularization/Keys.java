package com.pqixing.modularization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Keys {
    public static final String UPDATE_TIME = "update_time";
    public static final String UPDATE_LOG = "update_log";
    public static final String UPDATE_VERSION = "update_version";
    public static final String PREFIX_TO_MAVEN = "build://aar";
    public static final String PREFIX_LOG = "build://log";
    public static final String PREFIX_IDE_LOG = "ide://log";
    public static final String LOG_MODULE = "module";
    public static final String LOG_BRANCH = "branch";
    public static final String LOG_VERSION = "version";
    public static final String TXT_DPS_ANALYSIS = "DpsAnalysis.txt";
    public static final String TXT_DPS_REPORT = "DpsReport.txt";
    public static final String TXT_DPS_COMPARE = "DpsCompare.txt";
    /**
     * 版本号过滤
     */
    public static final String VERSION_FILTER = "version_filter";

    public static final String TAG = "//START.*?//END";

    /**
     * 一个tab空格
     */
    public static final String MAVEN = ".maven";
    /**
     * 一个tab空格
     */
    public static final String TAB = "      ";
    /**
     * 默认
     */
    public static final String DEFAULT = "DEFAULT";
    /**
     * 测试环境
     */
    public static final String TEST = "test";
    /**
     * 文档管理插件名称
     */
    public static final String NAME_DOC = "docment";
    /**
     * App插件名称
     */
    public static final String NAME_APP = "com.android.application";
    /**
     * library插件名称
     */
    public static final String NAME_LIBRARY = "com.android.library";
    /**
     * git插件名称
     */
    public static final String NAME_GIT = "manager";
    /**
     * 插件名称获取
     */
    public static final String NAME_PUGLIN = "plugin_name";
    /**
     * 代码分割线
     */
    public static final String CHARSET = "utf-8";
    /**
     * 代码分割线
     */
    public static final String SEPERATOR = "##";
    /**
     * 任务分组
     */
    public static final String GROUP_TASK = "modularization";
    /**
     * other任务分组
     */
    public static final String GROUP_OTHER = "other";
    /**
     * 远程配置项
     */
    public static final String REMOTE_CONFIG = "remoteConfig";
    /**
     * 全局配置名称
     */
    public static final String GLOBAL_CONFIG_NAME = "modularization.properties";
    /**
     * 全局隐藏配置名称
     */
    public static final String HIDE_CONFIG_NAME = "hide.properties";
    /**
     * 远程配置gradle
     */
    public static final String REMOTE_GRADLE = "remoteGradle";
    /**
     * 本地配置
     */
    public static final String LOCAL_GRADLE = "local.gradle";
    /**
     * 模块配置
     */
    public static final String CONFIG_MODULE = "moduleConfig";
    /**
     * 模块配置
     */
    public static final String CONFIG_DPS = "innerDps";

    /**
     * 模块配置
     */
    public static final String CONFIG_GIT = "gitConfig";
    /**
     * 强制更新配置
     */
    public static final String FOCUS_GRADLE = "focus.gradle";
    /**
     * 网络开头
     */
    public static final String PREFIX_NET = "http";


    /**
     * 主线版本依赖去除是
     */
    public static final String GROUP_MASTER = "com.modularization.master";
    /**
     * 分支分割标记
     */
    public static final String BRANCH_TAG = "-b-";
    /**
     * 默认初始版本号
     */
    public static final String VERSION_DEFAULT = "2.0.0";
    /**
     * 时间后缀
     */
    public static final String SUFFIX_STAMP = "-stamp";
    /**
     * 包名前缀
     */
    public static final String PREFIX_PKG = "auto";

    /**
     * 自动添加的代码tag
     */
    public static final String TAG_AUTO_ADD = "Auto Add By Modularization";
    /**
     * 清单文件名
     */
    public static final String MANIFEST = "AndroidManifest.xml";
    /**
     * 默认的gradle文件名
     */
    public static final String NAME_GRADLE_DEFAULT = "default.gradle";
    /**
     * Apk类型会自动生成默认入口类，方便反射获取
     */
    public static final String NAME_ENTER_CONFIG = "auto.com.pqixing.configs.Enter";

    /**
     * 批量上传全部工程文件
     */
    public static final String BATH_ALL = "dps2all";
    /**
     * 合并依赖到master分支
     */
    public static final String BATH_MASTER = "batch_master";
    /**
     * 合并依赖到release分支
     */
    public static final String BATH_RELEASE = "batch_release";

    /**
     * 上传日志记录
     */
    public static final String MAVEN_RECORD = "maven.record";
    /**
     * 空白标签
     */
    public static final String TAG_EMPTY = "emptyTag";

    /**
     * gradle.properteis文件
     */
    public static final String NAME_PRO_GRADLE = "gradle.properties";
    /**
     * 编译的缓存目录
     */
    public static final String ENV_BUILD_DIR = "buildDir";
    /**
     * 待操作的git名称
     */
    static String ENV_GIT_NAMES = "gitNames";
    /**
     * 对外输出路径
     */
    static String ENV_OUT_DIR = "outputDir";
    /**
     * git目标处理
     */
    static String ENV_GIT_TARGET = "target";
    /**
     * 基础分支名称
     */
    static String ENV_GIT_BASE_BRANCH = "baseBranchName";
    /**
     * git不存在的提示
     */
    static String TIP_GIT_NOT_EXISTS = "Git Not Exists";
    /**
     * 分支不存在
     */
    static String TIP_BRANCH_NOT_EXISTS = "Branch Not Exists";

    /**
     * git操作失败
     */
    static String TIP_GIT_MERGE_FAIL = "Git MERGE FAIL";
    /**
     * 运行Id
     */
    static String ENV_RUN_ID = "runId";
    /**
     * 模块的运行类型  app or library
     */
    static String ENV_BUILD_APP_TYPE = "appRunType";
    /**
     * App编译后的名称
     */
    static String ENV_BUILD_APP_NAME = "appBuildName";

    /**
     * 获取
     */
    public static final String RUN_TASK_ID = "run_task_id";
    /**
     * 合并结果分析
     */
    public static final String MERGE_RESULT = "merge.txt";
}