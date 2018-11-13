package com.pqixing.modularization.manager

import com.pqixing.modularization.base.BaseExtension

import org.gradle.api.Project

open class ManagerExtends(project: Project) : BaseExtension(project) {

    /**
     * 存放配置的git目录
     */
    var docGitUrl = ""
    var gitUserName = ""
    var gitPassWord = ""
    var branchName = "master"

    /**
     * 报名
     */
    var groupName = ""
    /**
     * 上传组件的Maven地址，下载地址请到Doc目录的Manger目录进行配置
     */
    var groupMaven = ""
    /**
     * 添加依赖地址，如果为空，默认使用groupMaven
     */
    var dependMaven: List<String> = arrayListOf()

    fun checkVail() {
        if (docGitUrl.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "docGitUrl can not be null!!!")
        if (groupName.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupName can not be null!!!")
        if (groupMaven.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupMaven can not be null!!!")
    }
}
