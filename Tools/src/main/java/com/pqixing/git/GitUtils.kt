package com.pqixing.git

import com.pqixing.Tools
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.*
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object GitUtils {
    lateinit var credentials: ICredential
    fun init(credentials: ICredential) {
        this.credentials = credentials
    }

    @JvmStatic
    fun clone(gitUrl: String, gitDir: File) = clone(gitUrl, gitDir, "master", Tools.logger)

    @JvmStatic
    fun clone(gitUrl: String, gitDir: File, branchName: String) = clone(gitUrl, gitDir, branchName, Tools.logger)

    @JvmStatic
    fun clone(gitUrl: String, gitDir: File, logger: ILog) = clone(gitUrl, gitDir, "master", logger)

    /**
     * clone
     * @param gitUrl
     * @param dirPath
     * @return
     */
    @JvmStatic
    fun clone(gitUrl: String, gitDir: File, branchName: String, logger: ILog): Git? {
        return Git.cloneRepository()
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(credentials.getUserName(), credentials.getPassWord()))
                .setURI(gitUrl).setDirectory(gitDir).setBranch(branchName)
                .setProgressMonitor(PercentProgress(logger))
                .call()
    }

    /**
     * 查找出对应的git根目录
     * @param dir
     * @return
     */
    @JvmStatic
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
    return this
}

fun <T> GitCommand<T>.execute(): T? = try {
    Tools.println("Git task -> $repository : ${javaClass.simpleName} ")
    val call = call()
    Tools.println("success ->  ${javaClass.simpleName}")
    call
} catch (e: Exception) {
    ///home/pqixing/Desktop/gradleProject/Root/Document/.git
    FileUtils.delete(File(repository.directory, "index.lock"))
    Tools.println(e.toString())
    null
}


