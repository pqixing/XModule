package com.pqixing.modularization.manager

import com.pqixing.modularization.Keys
import com.pqixing.modularization.gradle.base.BaseExtension
import com.pqixing.modularization.gradle.common.GlobalConfig
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 * 基础信息编译生成的
 */
class GitConfig extends BaseExtension {
    final String branchName
    final String revisionNum
    final String lastLog
    final boolean rootForGit
    final File gitDir

    /**
     * git用户名
     */
    static String userName
    /**
     * git密码
     */
    static String password
    /**
     * manager email信息
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
        gitDir = com.pqixing.modularization.gradle.utils.GitUtils.findGitDir(project.projectDir)
        if (project == project.rootProject || gitDir == null || !GlobalConfig.gitLog) {
            branchName = ""
            revisionNum = ""
            lastLog = ""
            return
        }
        def gitInfo = com.pqixing.modularization.gradle.utils.TextUtils.removeLineAndMark(com.pqixing.modularization.gradle.utils.GitUtils.run("manager println -1 HEAD --oneline --pretty=format:'%H::%D::%s'", gitDir)).split("::")
        if (gitInfo.length > 0)
            revisionNum = gitInfo[0].trim()
        if (gitInfo.length > 1)
            branchName = gitInfo[1].split(",")[0].replace("HEAD", "").replace("->", "").trim()
        if ("%D" == branchName) {
            branchName = com.pqixing.modularization.gradle.utils.TextUtils.removeLineAndMark(com.pqixing.modularization.gradle.utils.GitUtils.run("manager rev-parse --abbrev-ref HEAD", gitDir))
        }
        if(com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(branchName)){
            branchName = "master"
        }
        if (gitInfo.length > 2)
            lastLog = gitInfo[2]
        rootForGit = gitDir.absolutePath == project.projectDir.absolutePath
//        branchName = GitUtils.run("manager rev-parse --abbrev-ref HEAD", project.projectDir).trim()
//        revisionNum = GitUtils.run("manager rev-parse HEAD", project.projectDir).trim()
//        lastLog = GitUtils.run("manager println -1 --oneline ${revisionNum}", project.projectDir).trim()
        if (!rootForGit) {
            String dirLog = com.pqixing.modularization.gradle.utils.TextUtils.removeLineAndMark(com.pqixing.modularization.gradle.utils.GitUtils.run("manager println -1 HEAD --oneline --pretty=format:'%H' ${project.name}/", gitDir))
            //如果当前工程不是单独一个git，则使用目录的版本号作为最后的版本号
            if (!com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(dirLog)) {
                lastLog += " root revision:$revisionNum"
                revisionNum = dirLog
            }
        }
        com.pqixing.modularization.gradle.utils.Print.ln("revisionNum :$revisionNum branchName :$branchName  lastLog : $lastLog")

    }

    List<String> log(int num = 5) {
        List<String> logs = new LinkedList<>()
        StringBuilder item = null
        "manager println -$num".execute(null, project.projectDir)?.in?.getText(Keys.CHARSET)?.eachLine { line ->
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
