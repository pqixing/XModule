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

    var gitInfo: GitInfo? = null


    constructor(name: String, gitUrl: String, introduce: String, rootName: String) {
        this.name = name
        this.gitUrl = gitUrl
        this.introduce = introduce
        this.rootName = rootName
    }

    constructor() {}

    fun loadGitInfo(git: Git) {

    }

}

/**
 * git相关信息
 */
data class GitInfo(var commit: String = ""
                   , var author: String = ""
                   , var lastTime: String = ""
                   , var message: String = ""
                   , var branch: String = "") {
    var log: List<String>? = null
}
