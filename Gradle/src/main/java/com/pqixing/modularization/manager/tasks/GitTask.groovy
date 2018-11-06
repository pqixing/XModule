package com.pqixing.modularization.manager.tasks

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.Keys
import com.pqixing.modularization.gradle.common.GlobalConfig
import com.pqixing.modularization.manager.GitConfig
import com.pqixing.modularization.manager.GitProject

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

abstract class GitTask extends com.pqixing.modularization.base.BaseTask {
    /**
     * 待操作的目标git目录
     */
    Set<GitProject> targetGits
    String target
    String branchName
    String baseBranchName
    Set<String> excludeGit = [com.pqixing.modularization.gradle.utils.GitUtils.getNameFromUrl(GlobalConfig.docGitUrl)]

    @Override
    void start() {
        target = com.pqixing.modularization.gradle.utils.TextUtils.getSystemEnv(Keys.ENV_GIT_TARGET)
        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(target)) target = GlobalConfig.target

        branchName = GlobalConfig.branchName
        excludeGit += GlobalConfig.excludeGit

        targetGits = new HashSet<>()
        baseBranchName = com.pqixing.modularization.gradle.utils.TextUtils.getSystemEnv(Keys.ENV_GIT_BASE_BRANCH)
        if (com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(baseBranchName)) baseBranchName = "master"

    }

    @Override
    void runTask() {
        switch (target) {
            case "all":
                targetGits.addAll(GitConfig.allGitProjects)
                break
            case "include":
                Set<String> gitDirNames = new HashSet<>()
                project.rootProject.allprojects.each { p ->
                    String gitName = com.pqixing.modularization.gradle.utils.GitUtils.findGitDir(p.projectDir)?.name
                    if (!com.pqixing.modularization.gradle.utils.CheckUtils.isEmpty(gitName)) gitDirNames.add(gitName)
                }

                GitConfig.allGitProjects.each { p ->
                    if (gitDirNames.contains(p.name)) targetGits.add(p)
                }
                break
            case "system":
                Set<String> gitDirNames = new HashSet<>()
                com.pqixing.modularization.gradle.utils.TextUtils.getSystemEnv(Keys.ENV_GIT_NAMES)?.split(",")?.each {
                    if (it != null && !it.trim().isEmpty()) {
                        gitDirNames.add(it)
                    }
                }
                GitConfig.allGitProjects.each { p ->
                    boolean isGit = gitDirNames.contains(p.name)
                    if (!isGit) p.submodules.each {
                        it.each { sub ->
                            String sName = sub.toString().split("###")[0]

                            if (gitDirNames.contains(sName)) isGit = true
                        }
                    }
                    if (isGit) targetGits.add(p)
                }
                break
            default: break
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
        Map<String, String> handleResult = new HashMap<>()
        targetGits.each { p ->
            if (excludeGit.contains(p.name)) return
            String fullUrl = com.pqixing.modularization.gradle.utils.GitUtils.getFullGitUrl(p.gitUrl)
            com.pqixing.modularization.gradle.utils.Print.ln("GitTask $name -> start : $p.name $fullUrl ")
            String result = onGitProject(p.name, fullUrl, new File(project.rootDir.parentFile, p.name))
            if (Keys.TIP_GIT_NOT_EXISTS == result || Keys.TIP_BRANCH_NOT_EXISTS == result || Keys.TIP_GIT_MERGE_FAIL) {
                handleResult.put(p.name, result)
            } else {
                handleResult.put(p.name, "ok")
            }
            com.pqixing.modularization.gradle.utils.Print.ln("result -> : $result ")
        }
        com.pqixing.modularization.gradle.utils.Print.lnIde("${JSON.toJSONString(handleResult)}")
    }

}
