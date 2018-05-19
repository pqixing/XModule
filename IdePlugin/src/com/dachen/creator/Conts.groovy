package com.dachen.creator

class Conts {
    /**
     * 编译的缓存目录
     */
    public static final String  ENV_BUILD_DIR= "buildDir"
    /**
     * 待操作的git名称
     */
    static String ENV_GIT_NAMES = "gitNames"
    /**
     * 运行类型
     */
    static String ENV_RUN_TYPE = "runType"
    /**
     * git分支名称
     */
    static String ENV_GIT_BRANCH = "branchName"
    static String ENV_SILENT_LOG = "silentLog"
    static String ENV_UPDATE_BEFORE_SYNC = "updateBeforeSync"
    static String ENV_FOCUS_INCLUDES = "foucesIncludes"
    static String ENV_DEPENDENT_MODEL = "dependentModel"
    /**
     * git目标处理
     */
    static String ENV_GIT_TARGET = "target"
    /**
     * git不存在的提示
     */
    static String TIP_GIT_NOT_EXISTS = "Git Not Exists"
    /**
     * 分支不存在
     */
    static String TIP_BRANCH_NOT_EXISTS = "Branch Not Exists"
    /**
     * 运行Id
     */
    static String ENV_RUN_ID = "runId"
}
