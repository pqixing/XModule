package com.pqixing.modularization.manager

import org.eclipse.jgit.api.Git

/**
 * Created by pqixing on 17-12-7.
 */

class GitProject {
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

    var branch: String = "master"

    constructor(name: String, gitUrl: String, introduce: String, rootName: String) {
        this.name = name
        this.gitUrl = gitUrl
        this.introduce = introduce
        this.rootName = rootName
    }

    constructor()

    fun loadGitInfo(git: Git) {
        val repo = git.repository
        branch = repo.branch
        val command = git.log().setMaxCount(1)
        if (rootName != name) command.addPath(name)
        command.call().forEach { rev ->
            lastLog.author = rev.authorIdent.name
            lastLog.commitTime = rev.commitTime.toString()
            lastLog.message = rev.fullMessage
            lastLog.hash = rev.name
        }
    }
}


data class GitLog(var hash: String = "", var author: String = "", var message: String = "", var commitTime: String = "")