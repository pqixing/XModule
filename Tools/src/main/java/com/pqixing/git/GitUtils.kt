package com.pqixing.git

import com.pqixing.Tools
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import com.pqixing.tools.CheckUtils.isGitDir
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.*
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object GitUtils {
    lateinit var credentials: ICredential
    fun init(credentials: ICredential) {
        this.credentials = credentials
    }

    fun open(file: File?): Git? {
        if (file?.exists() == false) return null
        return try {
            Git.open(file)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * clone
     * @param gitUrl
     * @param dirPath
     * @return
     */
    fun clone(gitUrl: String, gitDir: File, branchName: String = "master"): Git? {
        val git = Git.cloneRepository()
                .setURI(gitUrl).setDirectory(gitDir).setBranch(branchName)
                .init().execute()
        if (git == null) {
            if (branchName != "master") return clone(gitUrl, gitDir, "master")
        } else {
            //如果名字不等（clone的分支不存在） checkout到master
            if (git.repository.branch != branchName) {
                git.checkout().setName("master")
                        .setCreateBranch(true)
                        .setStartPoint("refs/remotes/origin/master")
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .init().execute()
            }
        }
        return git
    }

    /**
     *刷新工程
     */
    fun pull(git: Git?): Boolean {
        git ?: return false
        try {
            Tools.println("${git.repository.directory.parentFile} start pull ->")
            git.pull().init().call()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 创建分支
     */
    fun createBranch(git: Git?, branchName: String): Boolean {
        git ?: return false
        //在同一个分支，不处理
        if (branchName == git.repository.branch) return true
        if (!pull(git)) return false

        val end = "/$branchName"
        val b = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().firstOrNull { it.name.endsWith(end) }
        if (b != null) {//如果已经存在分支，则直接切换过去
            return checkoutBranch(git, branchName, true)
        }
        git.checkout().setCreateBranch(true).setName(branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).init().execute()
        //创建分支成功，提交
        if (branchName == git.repository.branch) {
            git.push().init().execute()
            return true
        }

        return false
    }

    /**
     * 检查git是否clean状态
     */
    fun checkIfClean(git: Git?): Boolean = git?.status()?.call()?.apply {
        if (untracked.isNotEmpty()) Tools.println("${git.repository.directory.parentFile.name} checkIfClean  untracked -> $untracked")
        if (hasUncommittedChanges()) Tools.println("${git.repository.directory.parentFile.name} checkIfClean  uncommittedChanges -> $uncommittedChanges")
    }?.isClean ?: false

    /**
     * 检查分支是否一致
     */
    fun checkoutBranch(git: Git?, branchName: String, focusCheckOut: Boolean): Boolean {
        git ?: return false
        //在同一个分支，不处理
        if (branchName == git.repository.branch) return true
        val isClean = checkIfClean(git)
        //将本地修改文件存到暂存区
        if (!isClean) git.stashCreate().execute()

        var remote = false
        var tryCheckOut = tryCheckOut(git, branchName, git.branchList().call(), remote)
        if (!tryCheckOut) else {
            remote = true
            //本地没有分支时，先尝试更新一下，然后再进行处理
            git.pull().init().execute()
            tryCheckOut = tryCheckOut(git, branchName, git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call(), remote)
        }
        //还原本地的代码
        if (!isClean && !focusCheckOut) git.stashApply().execute()

        if (tryCheckOut) {
            Tools.println("Checkout ${if (remote) "remote" else "local"} branch $branchName")
        } else Tools.println("Can not find branch: $branchName ")

        return tryCheckOut
    }

    /**
     * 尝试切换分支
     */
    fun tryCheckOut(git: Git, branchName: String, ls: List<Ref>, remote: Boolean): Boolean {
        val end = "/$branchName"
        for (c in ls) {
            if (c.name.endsWith(end)) {
                val command = git.checkout().setName(branchName)
                if (remote) {
                    command.setCreateBranch(true)
                            .setStartPoint(c.name)
                            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                }
                command.init().execute()
                return true
            }
        }
        return false
    }

    /**
     * 查找出对应的git根目录
     * @param dir
     * @return
     */
    fun findGitDir(dir: File?): File? {
        var p = dir
        var g: File?
        while (p != null) {
            if (isGitDir(p)) return p
            p = p.parentFile
        }
        return null
    }

    inline fun isGitDir(dir: File): Boolean {
        val g = File(dir, ".git")
        return g.isDirectory && g.exists()
    }

    @JvmStatic
    fun getGitNameFromUrl(url: String?): String {
        url ?: return ""
        val s = url.lastIndexOf("/") + 1
        val e = url.indexOf(".", s)
        return url.substring(s, if (e == -1) url.length else e)
    }

}

fun <T> GitCommand<T>.init(provider: UsernamePasswordCredentialsProvider? = null): GitCommand<T> {
    if (this is TransportCommand<*, *>) {
        if (provider != null) setCredentialsProvider(provider)
        else setCredentialsProvider(UsernamePasswordCredentialsProvider(GitUtils.credentials.getUserName(), GitUtils.credentials.getPassWord()))
    }
    if (this is PullCommand) this.setProgressMonitor(PercentProgress())
    if (this is PushCommand) this.progressMonitor = PercentProgress()
    if (this is CloneCommand) this.setProgressMonitor(PercentProgress())
    if (this is CheckoutCommand) this.setProgressMonitor(PercentProgress())
    if (this is CheckoutCommand) this.setProgressMonitor(PercentProgress())
    return this
}

fun <T> GitCommand<T>.execute(): T? = try {
    Tools.println("Git task ->  ${javaClass.simpleName}")
    val call = call()
    val repo: Repository? = (call as? Git)?.repository ?: repository
    Tools.println("Git task end -> ${javaClass.simpleName} : ${repo?.branch} : ${repo?.directory?.parentFile?.name} \n      result -> $call")
    call
} catch (e: Exception) {
    ///home/pqixing/Desktop/gradleProject/Root/Document/.git
//    FileUtils.delete(File(repository.directory, "index.lock"))
    Tools.println(e.toString())
    null
}


