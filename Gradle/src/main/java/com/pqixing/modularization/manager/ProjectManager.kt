package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.git.GitUtils
import com.pqixing.modularization.ProjectInfo
import com.pqixing.modularization.wrapper.ProjectXml
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.gradle.api.Project
import java.io.File

object ProjectManager {
    val allProjects = HashMap<String, GitProject>()
    var hasInit = false
    fun checkVail() {
        if (hasInit) return
        ProjectXml.parse(FileManager.getProjectXml().readText(), allProjects)
        hasInit = true
    }

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project, plugin: ManagerPlugin, info: ProjectInfo) {
        checkVail()
        val buildDir = info.buildDir.toString().trim()
        if (buildDir.isNotEmpty()) project.buildDir = File(project.buildDir, buildDir)

        //不在配置文件的git工程，不进行管理
        val gitProject = allProjects[project.name] ?: return

        val projectDir = project.projectDir

        val rootDir = File(FileManager.codeRootDir, gitProject.rootName)

        val git = if (!projectDir.exists() || !checkRootDir(rootDir)) {//下载工程
            GitUtils.clone(gitProject.gitUrl, rootDir, info.curBranch)
        } else {
            Git.open(rootDir).apply { checkBranch(this, info) }
        }
        if (git != null) {
            gitProject.loadGitInfo(git)
            git.close()
        }
    }

    /**
     * 检查分支是否一致
     */
    private fun checkBranch(git: Git, info: ProjectInfo) {
        if (!info.syncBranch) return
        val branchName = info.curBranch
        //在同一个分支，不处理
        if (branchName == git.repository.branch) return


        val local = git.branchList().call()

        //强制切换，丢失本地未commit的文件
        if (info.focusCheckOut) {
            git.stashCreate().call()
            git.stashDrop().call()
        }

        val end = "/$branchName"
        for (c in local) {
            if (c.name.endsWith(end)) {
                git.checkout().setName(branchName).call()
                Tools.println("Checkout local branch $branchName")
                return
            }
        }
        val remote = git
                .branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call()
        for (c in remote) {
            if (c.name.endsWith(end)) {
                git.checkout().setName(branchName)
                        .setCreateBranch(true)
                        .setStartPoint(c.name)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .call()
                Tools.println("Checkout remote branch $branchName")
                return
            }
        }
        Tools.println("Can not find branch: $branchName ")
    }

    private fun checkRootDir(rootDir: File): Boolean {
        //如果根目录不是git目录,先删除
        if (!GitUtils.isGitDir(rootDir)) {
            rootDir.deleteOnExit()
            return false
        }
        return true
    }
}
