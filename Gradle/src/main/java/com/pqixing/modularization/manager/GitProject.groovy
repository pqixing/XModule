package com.pqixing.modularization.manager
/**
 * Created by pqixing on 17-12-7.
 */

class GitProject extends com.pqixing.modularization.base.BaseExtension {
    /**
     * git工程名称,指目录名称
     */
    String name
    /**
     * 介绍
     */
    String introduce
    /**
     * git地址
     */
    String gitUrl
    /**
     * 包含的子项目,默认格式  name###introduce
     */
    List<String> submodules = []


    GitProject() {
        super(com.pqixing.modularization.base.BasePlugin.rootProject)
    }

    @Override
    public String toString() {
        return "GitProject{" +
                "name='" + name + '\'' +
                ", introduce='" + introduce + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", submodules=" + submodules +
                '}';
    }
}
