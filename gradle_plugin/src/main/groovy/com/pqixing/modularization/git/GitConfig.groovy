package com.pqixing.modularization.git

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Print
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

    GitConfig(Project project) {
        super(project)
        File gitDir = GitUtils.findGitDir(project.projectDir)
        if (gitDir == null || !GlobalConfig.gitLog) {
            branchName = ""
            revisionNum = ""
            lastLog = ""
            return
        }
        def gitInfo = GitUtils.run("git log -1 HEAD --oneline --pretty=format:'%H::%D::%s'", gitDir).trim().split("::")
        if (gitInfo.length > 0)
            revisionNum = gitInfo[0]
        if (gitInfo.length > 1)
            branchName = gitInfo[1].split(",")[0].replace("HEAD", "").replace("->", "").trim()
        if ("%D" == branchName) {
            branchName = GitUtils.run("git rev-parse --abbrev-ref HEAD", gitDir).trim()
        }
        if (gitInfo.length > 2)
            lastLog = gitInfo[2]

//        branchName = GitUtils.run("git rev-parse --abbrev-ref HEAD", project.projectDir).trim()
//        revisionNum = GitUtils.run("git rev-parse HEAD", project.projectDir).trim()
//        lastLog = GitUtils.run("git log -1 --oneline ${revisionNum}", project.projectDir).trim()
        if (gitDir.absolutePath != project.projectDir.absolutePath) {
            String dirLog = GitUtils.run("git log -1 HEAD --oneline --pretty=format:'%H' ${project.name}/", gitDir).trim()
            //如果当前工程不是单独一个git，则使用目录的版本号作为最后的版本号
            if (!CheckUtils.isEmpty(dirLog)) {
                lastLog += " root revision:$revisionNum"
                revisionNum = dirLog
            }
        }
        Print.ln("revisionNum :$revisionNum branchName :$branchName  lastLog : $lastLog")

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