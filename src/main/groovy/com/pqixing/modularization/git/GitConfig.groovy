package com.pqixing.modularization.git

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseExtension
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
    String userName
    /**
     * git密码
     */
    String password
    /**
     * git email信息
     */
    String emal

    /**
     * 全部的git工程
     */
    List<GitProject> allGitProjects = []

    GitConfig(Project project) {
        super(project)
        branchName = "git rev-parse --abbrev-ref HEAD".execute(null, project.projectDir).getText(Keys.CHARSET)
        revisionNum = "git rev-parse HEAD".execute(null, project.projectDir).getText(Keys.CHARSET).trim()
        lastLog = "git branch -vv".execute(null, project.projectDir).getText(Keys.CHARSET).find {
            it.startsWith("*")
        }
    }

    List<String> log(int num = 5) {
        List<String> logs = new LinkedList<>()
        StringBuilder item = null
        "git log -$num".execute(null, project.projectDir).text.eachLine { line ->
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
