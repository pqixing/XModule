package com.pqixing.modularization.dependent
/**
 * Created by pqixing on 17-12-25.
 * 简单半的依赖处理，用于依赖层级判断使用
 */

class SimpleModule {
    public static final int TYPE_MASTER = 0
    public static final int TYPE_BRANCH = 1
    public static final int TYPE_LOCAL = 2

    String moduleName
    /**
     * 0 master仓库 1 分支仓库 2 本地依赖
     */
    int type
    String branchName = ""
    int level

    SimpleModule(String moduleName) {
        this.moduleName = moduleName
    }

    @Override
    public String toString() {
        return level + "  " + moduleName + ", type=" + (type == TYPE_LOCAL ? "local" : branchName) + "\n"
    }
}
