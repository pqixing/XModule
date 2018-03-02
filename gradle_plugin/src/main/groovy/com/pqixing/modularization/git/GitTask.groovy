package com.pqixing.modularization.git

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Print

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

abstract class GitTask extends BaseTask {
    GitConfig gitConfig
    /**
     * 待操作的目标git目录
     */
    Set<GitProject> targetGits
    String target = "include"
    String checkout = "master"

    @Override
    void start() {
        gitConfig = wrapper.getExtends(GitConfig)
        target = gitConfig.target
        checkout = gitConfig.checkout
        targetGits = new HashSet<>()
    }

    @Override
    void runTask() {
        switch (target) {
            case "all":
                targetGits.addAll(GitConfig.allGitProjects)
                break
            default:
                Set<String> gitDirNames = new HashSet<>()
                project.rootProject.allprojects.each { p ->
                    String gitName = GitUtils.findGitDir(p.projectDir)?.name
                    if (!CheckUtils.isEmpty(gitName)) gitDirNames.add(gitName)
                }

                GitConfig.allGitProjects.each { p ->
                    if (gitDirNames.contains(p.name)) targetGits.add(p)
                }
                break
        }
    }
    /**
     * 执行git命令的方法
     * @param gitName
     * @param gitUrl
     * @param gitDir
     */
    abstract String onGitProject(String gitName, String gitUrl, File gitDir)

    @Override
    void end() {
        targetGits.each { p ->
            if (gitConfig.excludeGit.contains(p.name)) return
            String fullUrl = GitUtils.getFullGitUrl(p.gitUrl)
            Print.ln("GitTask $name -> start : $p.name $fullUrl ")
            String result = onGitProject(p.name, fullUrl, new File(project.rootDir.parentFile, p.name))
            Print.ln("GitTask $name -> result : $result ")
        }
    }
}
