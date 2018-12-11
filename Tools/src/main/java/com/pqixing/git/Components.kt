package com.pqixing.git

import org.eclipse.jgit.api.Git

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
     * 该组件类型
     */
    var type: String = TYPE_LIBRARY

    var hasInit = false

    constructor(name: String, gitUrl: String, introduce: String, rootName: String, type: String) {
        this.name = name
        this.gitUrl = gitUrl
        this.introduce = introduce
        this.rootName = rootName
        this.type = type
    }

    fun loadGitInfo(git: Git) {
        val repo = git.repository
        lastLog.branch = repo.branch
        val command = git.log().setMaxCount(1)
        if (rootName != name) command.addPath(name)
        command.call().forEach { rev ->
            lastLog.author = rev.authorIdent.name
            lastLog.commitTime = rev.commitTime
            lastLog.message = rev.fullMessage
            lastLog.hash = rev.name
        }
        hasInit = true
    }

    companion object {
        val TYPE_LIBRARY = "library"
        val TYPE_APPLICATION = "application"
        val TYPE_LIBRARY_API = "library_api"
        val TYPE_LIBRARY_LOCAL = "library_local"
        val TYPE_DOCUMENT = "document"
    }
}


data class GitLog(var hash: String = "", var author: String = "", var message: String = "", var commitTime: Int = 0, var branch: String = "master")