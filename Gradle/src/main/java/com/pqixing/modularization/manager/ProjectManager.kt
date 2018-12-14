package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.ProjectInfo
import com.pqixing.Tools.rootDir
import com.pqixing.git.*
import com.pqixing.help.XmlHelper
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.Project
import java.io.File

object ProjectManager {
    var rootBranch = ""

    val allComponents = HashMap<String, Components>()
    val gitForProject = HashMap<String, Git>()
    var hasInit = false
    fun findComponent(name: String) = allComponents[name]!!
    fun checkVail() {
        if (hasInit) return
        XmlHelper.parseProjectXml(FileManager.getProjectXml(), allComponents)
        hasInit = true
    }

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project, info: ProjectInfo): Components? {
        checkVail()
        val buildDir = info.buildDir.toString().trim()
        //重新设置build 目录
        project.buildDir = File(project.buildDir, if (buildDir.isEmpty()) "default" else buildDir)

        //不在配置文件的git工程，不进行管理
        val gitProject = allComponents[project.name] ?: return null
        if (gitProject.hasInit) return gitProject//已经初始化，不再重复初始化
        val projectDir = project.projectDir

        val rootDir = File(FileManager.codeRootDir, gitProject.rootName)
        var git = gitForProject[rootDir.absolutePath]
        if (git == null) {
            git = initGit(projectDir, rootDir, gitProject.gitUrl, info)
        }

        if (git != null) {
            gitForProject[rootDir.absolutePath] = git
            gitProject.loadGitInfo(git)
        }
        return gitProject
    }

    private fun initGit(projectDir: File, rootDir: File, gitUrl: String, info: ProjectInfo): Git? {
        val git = if (!projectDir.exists() || !checkRootDir(projectDir, rootDir)) {//下载工程
            GitUtils.clone(gitUrl, rootDir, rootBranch)
        } else {
            Git.open(rootDir)
        }
        if (git != null) {
            checkBranch(git, info)
            if (info.updateCode) git.pull().init().execute()
        }
        return git
    }

    /**
     * 检查分支是否一致
     */
    private fun checkBranch(git: Git, info: ProjectInfo) {
        if (!info.syncBranch) return
        val branchName = rootBranch
        Tools.println("checkBranch ${git.repository} -> branch : $branchName target -> $rootBranch")
        //在同一个分支，不处理
        if (branchName == git.repository.branch) return


        val local = git.branchList().call()
        val end = "/$branchName"
        for (c in local) {
            if (c.name.endsWith(end)) {
                //强制切换，丢失本地未commit的文件
                if (info.focusCheckOut) {
                    git.stashCreate().call()
                    git.stashDrop().call()
                }
                git.checkout().setName(branchName).init().execute()
                Tools.println("Checkout local branch $branchName")
                return
            }
        }
        //本地没有分支时，先尝试更新一下，然后再进行处理
        git.pull().init().execute()
        val remote = git
                .branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call()
        for (c in remote) {
            if (c.name.endsWith(end)) {
                //强制切换，丢失本地未commit的文件
                if (info.focusCheckOut) {
                    git.stashCreate().call()
                    git.stashDrop().call()
                }
                git.checkout().setName(branchName)
                        .setCreateBranch(true)
                        .setStartPoint(c.name)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .init().execute()
                Tools.println("Checkout remote branch $branchName")
                return
            }
        }
        Tools.println("Can not find branch: $branchName ")
    }

    private fun checkRootDir(projectDir: File, rootDir: File): Boolean {
        //如果根目录不是git目录,先删除
        if (!GitUtils.isGitDir(rootDir) || projectDir.listFiles().size < 3) {
            FileUtils.delete(rootDir)
            return false
        }
        return true
    }
}
