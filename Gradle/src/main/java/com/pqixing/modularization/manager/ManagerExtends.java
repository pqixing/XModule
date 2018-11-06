package com.pqixing.modularization.manager;

import com.pqixing.modularization.base.BaseExtension;

import org.gradle.api.Project;

public class ManagerExtends extends BaseExtension {

    /**
     * 存放配置的git目录
     */
    String docGitUrl = "";
    String gitUserName = "";
    String gitPassWord = "";


    public ManagerExtends(Project project) {
        super(project);
    }
}
