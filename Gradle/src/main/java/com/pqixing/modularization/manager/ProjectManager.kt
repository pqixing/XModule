package com.pqixing.modularization.manager

import com.pqixing.ProjectInfo
import com.pqixing.git.Components
import com.pqixing.git.GitUtils
import com.pqixing.git.execute
import com.pqixing.git.init
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File
import java.util.*

object ProjectManager : OnClear {
    init {
        BasePlugin.addClearLister(this)
    }

    override fun clear() {
        hasInit = false
        gitForProject.forEach {
            val gitPath = it.value.repository?.directory?.absolutePath
            it.value.close()
            if (gitPath != null)//执行完成后，删除index.lock文件，防止其他集成无法操作
                FileUtils.delete(File(gitPath, "index.lock"))
        }
        gitForProject.clear()
        allComponents.clear()
        errorBranchProject.clear()
        rootBranch = ""

    }

    var rootBranch = ""
    var errorBranchProject = HashSet<String>()

    private val allComponents = HashMap<String, Components>()
    private val gitForProject = HashMap<String, Git>()

    var projectRoot: File = ManagerPlugin.getManagerPlugin().projectDir
    var hasInit = false

    fun findAllComponent(): Set<Components> {
        checkVail()
        return allComponents.values.toSet()
    }

    /**
     * 查出所有工程的Git目录
     */
    fun findAllGitPath(): Map<String, File> {
        checkVail()
        return allComponents.map { it.value.gitUrl to File(FileManager.codeRootDir, it.value.rootName) }.toMap()
    }

    fun findComponent(name: String): Components? {
        checkVail()
        return allComponents[name]
    }

    fun setGit(name: String, git: Git?) {
        git ?: return
        gitForProject[name] = git
    }

    fun findGit(name: String): Git? {
        var git = gitForProject[name]
        if (git == null) {
            git = Git.open(File(name))
            if (git != null) gitForProject[name] = git
        }
        return git
    }

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
        return if (!projectDir.exists() || !checkRootDir(projectDir, rootDir)) {//下载工程
            GitUtils.clone(gitUrl, rootDir, rootBranch)
        } else {
            Git.open(rootDir)
        }?.apply {
            if (info.syncBranch) {
                val checkout = GitUtils.checkoutBranch(this, rootBranch, info.focusCheckOut)
                //如果切换分支失败，做个标记
                if (!checkout) errorBranchProject.add("${rootDir.name}/${this.repository.branch}")
            }
            if (info.updateCode) this.pull().init().execute()
        }
    }

    fun checkRootDir(projectDir: File, rootDir: File): Boolean {
        //如果根目录不是git目录,先删除
        if (!GitUtils.isGitDir(rootDir) || projectDir.listFiles().size < 3) {
            FileUtils.delete(rootDir)
            return false
        }
        return true
    }
}
