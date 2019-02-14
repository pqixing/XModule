package com.pqixing.git

import java.io.File

/**
 * Created by pqixing on 17-12-7.
 * 工程组件
 */

class Components {
    /**
     * git工程名称,指目录名称
     */
    var name: String = ""

    /**
     * git地址
     */
    var gitUrl: String = ""

    /**
     *
     */
    var introduce: String = ""
    /**
     * 根目录的名称
     */
    var rootName: String = ""

    /**
     * 日志相关信息
     */
    var lastLog: GitLog = GitLog()
    /**
     * 是否是子级工程
     */
    var child = false
    /**
     * 该组件类型
     */
    var type: String = TYPE_LIBRARY

    var hasInit = false

    fun getPath() = if (child) "$rootName/$name" else rootName

    constructor(name: String, gitUrl: String, introduce: String, rootName: String, type: String) {
        this.name = name
        this.gitUrl = gitUrl
        this.introduce = introduce
        this.rootName = rootName
        this.type = type
    }

    override fun toString(): String {
        return "Components(name='$name', gitUrl='$gitUrl', introduce='$introduce', rootName='$rootName', type='$type', hasInit=$hasInit)"
    }


    companion object {
        val TYPE_LIBRARY = "library"
        val TYPE_APPLICATION = "application"
        val TYPE_LIBRARY_API = "library_api"
        val TYPE_LIBRARY_LOCAL = "library_local"
        val TYPE_LIBRARY_SYNC = "library_sync"
        val TYPE_DOCUMENT = "document"
    }
}


data class GitLog(var hash: String = "", var author: String = "", var message: String = "", var commitTime: Int = 0, var branch: String = "master") {
    lateinit var gitDir: File
}