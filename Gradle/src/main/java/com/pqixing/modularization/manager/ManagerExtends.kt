package com.pqixing.modularization.manager

import com.pqixing.modularization.base.BaseExtension

import org.gradle.api.Project

open class ManagerExtends(project: Project) : BaseExtension(project) {

    /**
     * 存放配置的git目录
     */
    internal var docGitUrl = ""
    internal var gitUserName = ""
    internal var gitPassWord = ""
    internal var branchName = "master"

    /**
     * 报名
     */
    internal var groupName = ""
    /**
     * 上传组件的Maven地址，下载地址请到Doc目录的Manger目录进行配置
     */
    internal var groupMaven = ""

    fun checkVail() = when {
        docGitUrl.isEmpty() -> ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "docGitUrl can not be null!!!")
        groupName.isEmpty() -> ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupName can not be null!!!")
        groupMaven.isEmpty() -> ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupMaven can not be null!!!")
        else -> Unit
    }
}
