package com.pqixing.modularization.git

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.utils.GitUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 * 基础信息编译生成的
 */
class GitConfig extends BaseExtension {
    final String branchName
    final String revisionNum
    final String lastLog

    /**
     * git用户名
     */
    static String userName
    /**
     * git密码
     */
    static String password
    /**
     * git email信息
     */
    static String email
    static String baseGitUrl
    /**
     * 全部的git工程
     */
    static List<GitProject> allGitProjects = []
    static Map<String, String> localProject

    /**
     * 默认git操作目标
     * include，all
     */
    String target = "include"
    /**
     * 分支名称
     */
    String checkout = "master"

    GitConfig(Project project) {
        super(project)
        branchName = GitUtils.run("git rev-parse --abbrev-ref HEAD", project.projectDir)?.trim() ?: ""
        revisionNum = GitUtils.run("git rev-parse HEAD", project.projectDir)?.trim() ?: ""
        def lines = GitUtils.run("git checkout -vv", project.projectDir)?.readLines()
        if (lines != null) for (String l : lines) {
            if (l.startsWith("*")) lastLog = l.trim()
        }
        if (lastLog == null) lastLog = ""
    }

    List<String> log(int num = 5) {
        List<String> logs = new LinkedList<>()
        StringBuilder item = null
        "git log -$num".execute(null, project.projectDir)?.in?.getText(Keys.CHARSET)?.eachLine { line ->
            if (line.startsWith("commit ")) {
                if (item != null) logs += item.toString()
                item = new StringBuilder()
            }
            item.append("$line\n")
        }
        logs += item.toString()
        return logs
    }
}
