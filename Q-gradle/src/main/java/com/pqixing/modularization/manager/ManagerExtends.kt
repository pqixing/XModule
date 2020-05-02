package com.pqixing.modularization.manager

import com.alibaba.fastjson.JSON
import com.pqixing.Config
import com.pqixing.Tools
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.manager.FileManager.rootDir
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import org.gradle.api.Project
import java.io.File
import java.util.*

open class ManagerExtends(project: Project) : BaseExtension(project) {

    /**
     * 上传到maven的用户名
     */
    var mavenUserName = ""
        get() = if (field.isNotEmpty()) field else config.userName

    /**
     * 上传到maven的密码
     */
    var mavenPassWord = ""
        get() = if (field.isNotEmpty()) field else config.passWord
        set(value) {
            when {
                value.isEmpty() -> field = value
                value.startsWith("sk:") -> field =ResultUtils.base64Decode(value.substring(3))
                else -> {
                    field = value
                    Tools.println("Warming!!! suggest config mavenPassWord $value -> ${"sk:"+ResultUtils.base64Encode(value)}")
                }
            }
        }

    /**
     * 文档工程地址
     */
    var docRepoUrl = ""

    /**
     *
     */
    var docRepoBranch = ""

    /**
     * 文档工程用户名,不配置默认使用ProjectInfo中
     */
    var docRepoUser = ""
        get() = if (field.isNotEmpty()) field else config.userName
    var docRepoPsw = ""
        get() = if (field.isNotEmpty()) field else config.passWord
        set(value) {
            when {
                value.isEmpty() -> field = value
                value.startsWith("sk:") -> field =ResultUtils.base64Decode(value.substring(3))
                else -> {
                    field = value
                    Tools.println("Warming!!! suggest config docRepoPsw $value -> ${"sk:"+ResultUtils.base64Encode(value)}")
                }
            }
        }

    /**
     * 包名
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

    /**
     * 支持忽略版本的模块
     */
    var emptyVersions = mutableListOf<String>()

    /**
     * 是否支持忽略版本号
     */
    var allowEmptyVerion = true

    fun checkVail() {
        if (dependMaven.isEmpty()) dependMaven.add(groupMaven)
        if (docRepoUrl.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "docRepoUrl can not be null!!!")
//        if (docRepoUser.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "docRepoUser can not be null!!!")
//        if (docRepoPsw.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "docRepoPsw can not be null!!!")

//        if (groupName.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupName can not be null!!!")
//        if (groupMaven.isEmpty()) ExceptionManager.thow(ExceptionManager.EXCEPTION_SYNC, "groupMaven can not be null!!!")
    }

    var config: com.pqixing.Config = try {
        val parseClass = GroovyClassLoader().parseClass(File(rootDir, FileNames.USER_CONFIG))
        JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), Config::class.java)
    } catch (e: Exception) {
        Config()
    }.apply { loadProjectInfo(this) }

    /**
     * 从系统配置中加载对应的变量
     */
    private fun loadProjectInfo(pi: Config) {
        pi.javaClass.fields.forEach {
            val value = TextUtils.getSystemEnv(it.name) ?: return@forEach
            try {
                it.isAccessible = true
                when (it.type) {
                    Boolean::class.java -> it.setBoolean(pi, value.toBoolean())
                    String::class.java -> it.set(pi, value)
                }
            } catch (e: Exception) {
                Tools.println("loadProjectInfo Exception -> $e")
            }
        }
    }
}
