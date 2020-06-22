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
    /**
     * git 任务操作的目标分支
     */
    public static final String opBranch = "opBranch";
    /**
     *打标签时,需要tag的分支
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
    public static final String XML_PROJECT = "basic/project.xml";
}
