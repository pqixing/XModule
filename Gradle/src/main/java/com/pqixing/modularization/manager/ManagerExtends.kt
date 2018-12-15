package com.pqixing.modularization.manager

import com.pqixing.modularization.base.BaseExtension

import org.gradle.api.Project

open class ManagerExtends(project: Project) : BaseExtension(project) {

    /**
     * 上传到maven的用户名
     */
    var mavenUserName = ""
    /**
     * 上传到maven的密码
     */
    var mavenPassWord = ""

    /**
     * 指定同步的分支，不指定时，取当前CodeManager的分支
     */
    var branch = "master"

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
    var dependMaven: MutableList<String> = arrayListOf()
    /**
     * 依赖匹配传递，如果当前分支无对应依赖包，则按照
     */
    var matchingFallbacks: MutableList<String> = arrayListOf("master")
    /**
     * 默认基础版本，如果ToMaven或者是依赖时没有配置，默认使用1.0
     */
    var baseVersion = "1.0"

    fun checkVail() {
        if (dependMaven.isEmpty()) dependMaven.add(groupMaven)
        if (groupName.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupName can not be null!!!")
        if (groupMaven.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupMaven can not be null!!!")
    }
}
