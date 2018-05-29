package com.pqixing.modularization.git

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.utils.TextUtils


/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

abstract class GitTask extends BaseTask {
    /**
     * 待操作的目标git目录
     */
    Set<GitProject> targetGits
    String target
    String branchName
    String baseBranchName
    Set<String> excludeGit = [GitUtils.getNameFromUrl(GlobalConfig.docGitUrl)]

    @Override
    void start() {
        target = TextUtils.getSystemEnv(Keys.ENV_GIT_TARGET)
        if (CheckUtils.isEmpty(target)) target = GlobalConfig.target

        branchName = GlobalConfig.branchName
        excludeGit += GlobalConfig.excludeGit

        targetGits = new HashSet<>()
        baseBranchName = TextUtils.getSystemEnv(Keys.ENV_GIT_BASE_BRANCH)
        if (CheckUtils.isEmpty(baseBranchName)) baseBranchName = branchName

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
                    String gitName = GitUtils.findGitDir(p.projectDir)?.name
                    if (!CheckUtils.isEmpty(gitName)) gitDirNames.add(gitName)
                }

                GitConfig.allGitProjects.each { p ->
                    if (gitDirNames.contains(p.name)) targetGits.add(p)
                }
                break
            case "system":
                Set<String> gitDirNames = new HashSet<>()
                TextUtils.getSystemEnv(Keys.ENV_GIT_NAMES)?.split(",")?.each {
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
            String fullUrl = GitUtils.getFullGitUrl(p.gitUrl)
            Print.ln("GitTask $name -> start : $p.name $fullUrl ")
            String result = onGitProject(p.name, fullUrl, new File(project.rootDir.parentFile, p.name))
            if (Keys.TIP_GIT_NOT_EXISTS == result || Keys.TIP_BRANCH_NOT_EXISTS == result || Keys.TIP_GIT_MERGE_FAIL) {
                handleResult.put(p.name, result)
            } else {
                handleResult.put(p.name, "ok")
            }
            Print.ln("result -> : $result ")
        }
        Print.lnIde("${JSON.toJSONString(handleResult)}")
    }

}
