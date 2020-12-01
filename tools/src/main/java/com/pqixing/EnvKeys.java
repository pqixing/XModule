package com.pqixing;

public class EnvKeys {
    /**
     * 读取env
     */
    public static final String buildApkType = "buildApkType";
    /**
     * 从系统env中读取
     */
    public static final String buildApkPath = "buildApkPath";

    public static final String seperator = ".s.";
    /**
     * git 任务操作的目标分支
     */
    public static final String opBranch = "opBranch";
    /**
     * 打标签时,需要tag的分支
     */
    public static final String tagBranch = "tagBranch";
    /**
     * 运行类型,gradle,plugin,ide
     */
    public static final String syncType = "syncType";
    /**
     * 部分重要操作需要的密码
     */
    public static final String screctKey = "screctKey";

    /**
     * toMaven的检查
     */
    public static final String toMavenUnCheck = "toMavenUnCheck";
    /**
     * 模板目录
     */
    public static final String BASIC = "basic";
    public static final String BASIC_LOG = "basic_log";
    public static final String BASIC_TAG = "basic_tag";
    public static final String XML_META = "maven-metadata.xml";
    public static final String XML_MANIFEST = "basic/manifest.xml";
    /**
     * 配置文件
     */
    public static final String USER_CONFIG = "Config.java";

    /**
     * 一个tab空格度
     */
    public static final String XMODULE = "px";


    /**
     * gitIgonre
     */
    public static final String GIT_IGNORE = ".gitignore";
    /**
     * 依赖输出路径
     */
    public static final String GRADLE_DEPENDENCIES = "depend.gradle";
}
