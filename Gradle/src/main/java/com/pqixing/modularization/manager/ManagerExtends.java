package com.pqixing.modularization.manager;

import com.pqixing.modularization.base.BaseExtension;

import org.gradle.api.Project;

public class ManagerExtends extends BaseExtension {

    /**
     * 存放配置的git目录
     */
    String docGitUrl = "";
    /**
     * 本地存放的git目录名称
     */
    final String docDir = "Document";
    String gitUserName = "";
    String gitPassWord = "";
    String branchName = "master";

    /**
     * 报名
     */
    String groupName = "";
    /**
     * 上传组件的Maven地址，下载地址请到Doc目录的Manger目录进行配置
     */
    String groupMaven = "";


    public ManagerExtends(Project project) {
        super(project);
    }
}
