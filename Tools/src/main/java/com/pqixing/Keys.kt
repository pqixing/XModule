package com.pqixing

internal object Keys {
    /**
     * 一个tab空格
     */
    val MODURIZATION = "modurization"
    /**
     * 一个tab空格
     */
    val MAVEN = ".maven"
    /**
     * 一个tab空格
     */
    val TAB = "      "
    /**
     * 默认
     */
    val DEFAULT = "DEFAULT"
    /**
     * 测试环境
     */
    val TEST = "test"
    /**
     * 文档管理插件名称
     */
    val NAME_DOC = "docment"
    /**
     * App插件名称
     */
    val NAME_APP = "application"
    /**
     * library插件名称
     */
    val NAME_LIBRARY = "library"
    /**
     * git插件名称
     */
    val NAME_GIT = "manager"
    /**
     * 插件名称获取
     */
    val NAME_PUGLIN = "plugin_name"
    /**
     * 代码分割线
     */
    val CHARSET = "utf-8"
    /**
     * 代码分割线
     */
    val SEPERATOR = "##"
    /**
     * 任务分组
     */
    val GROUP_TASK = "modularization"
    /**
     * other任务分组
     */
    val GROUP_OTHER = "other"
    /**
     * 远程配置项
     */
    val REMOTE_CONFIG = "remoteConfig"
    /**
     * 全局配置名称
     */
    val GLOBAL_CONFIG_NAME = "modularization.properties"
    /**
     * 全局隐藏配置名称
     */
    val HIDE_CONFIG_NAME = "hide.properties"
    /**
     * 远程配置gradle
     */
    val REMOTE_GRADLE = "remoteGradle"
    /**
     * 本地配置
     */
    val LOCAL_GRADLE = "local.gradle"
    /**
     * 模块配置
     */
    val CONFIG_MODULE = "moduleConfig"

    /**
     * 模块配置
     */
    val CONFIG_GIT = "gitConfig"
    /**
     * 强制更新配置
     */
    val FOCUS_GRADLE = "focus.gradle"
    /**
     * 网络开头
     */
    val PREFIX_NET = "http"

    /**
     * gitIgonre
     */
    val GIT_IGNORE = ".gitignore"
    /**
     * 依赖输出路径
     */
    val DIR_DEPENDENT = "dependencies"
    /**
     * Android依赖路径
     */
    val FILE_ANDROID_DP = "android.dp"
    /**
     * 内部依赖版本日志
     */
    val FILE_INNER_DP = "inner.dp"
    /**
     * 当前最新的版本号
     */
    val FILE_VERSION_DP = "version.dp"
    /**
     * 版本号控制
     */
    val DIR_VERSIONS = "versions"
    /**
     * 排序过后的以来关系
     */
    val FILE_SORT_DP = "level.dp"
    /**
     * 依赖版本号名称
     */
    val FILE_VERSION = "versions.properties"

    /**
     * 主线版本依赖去除是
     */
    val GROUP_MASTER = "com.modularization.master"
    /**
     * 分支分割标记
     */
    val BRANCH_TAG = "-b-"
    /**
     * 默认初始版本号
     */
    val VERSION_DEFAULT = "2.0.0"
    /**
     * 时间后缀
     */
    val SUFFIX_STAMP = "-stamp"
    /**
     * 包名前缀
     */
    val PREFIX_PKG = "auto"

    /**
     * 自动添加的代码tag
     */
    val TAG_AUTO_ADD = "Auto Add By Modularization"
    /**
     * 清单文件名
     */
    val MANIFEST = "AndroidManifest.xml"
    /**
     * 默认的gradle文件名
     */
    val NAME_GRADLE_DEFAULT = "default.gradle"
    /**
     * Apk类型会自动生成默认入口类，方便反射获取
     */
    val NAME_ENTER_CONFIG = "auto.com.pqixing.configs.Enter"

    /**
     * 批量上传全部工程文件
     */
    val BATH_ALL = "dps2all"
    /**
     * 合并依赖到master分支
     */
    val BATH_MASTER = "batch_master"
    /**
     * 合并依赖到release分支
     */
    val BATH_RELEASE = "batch_release"
    /**
     * 隐藏的导入文件，批量上传时使用
     */
    val TXT_HIDE_INCLUDE = "hideInclude.kt"

    /**
     * 上传日志记录
     */
    val MAVEN_RECORD = "maven.record"
    /**
     * 空白标签
     */
    val TAG_EMPTY = "emptyTag"

    /**
     * gradle.properteis文件
     */
    val NAME_PRO_GRADLE = "gradle.properties"
    /**
     * 编译的缓存目录
     */
    val ENV_BUILD_DIR = "buildDir"
    /**
     * 待操作的git名称
     */
    var ENV_GIT_NAMES = "gitNames"
    /**
     * 对外输出路径
     */
    var ENV_OUT_DIR = "outputDir"
    /**
     * git目标处理
     */
    var ENV_GIT_TARGET = "target"
    /**
     * 基础分支名称
     */
    var ENV_GIT_BASE_BRANCH = "baseBranchName"
    /**
     * git不存在的提示
     */
    var TIP_GIT_NOT_EXISTS = "Git Not Exists"
    /**
     * 分支不存在
     */
    var TIP_BRANCH_NOT_EXISTS = "Branch Not Exists"

    /**
     * git操作失败
     */
    var TIP_GIT_MERGE_FAIL = "Git MERGE FAIL"
    /**
     * 运行Id
     */
    var ENV_RUN_ID = "runId"
    /**
     * 模块的运行类型  app or library
     */
    var ENV_BUILD_APP_TYPE = "appRunType"
    /**
     * App编译后的名称
     */
    var ENV_BUILD_APP_NAME = "appBuildName"
    /**
     * 运行类型
     */
    var ENV_RUN_TYPE = "runType"
}