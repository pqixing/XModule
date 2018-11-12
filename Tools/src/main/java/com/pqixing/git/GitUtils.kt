package com.pqixing.git

import com.pqixing.Tools
import com.pqixing.Tools.logger
import com.pqixing.interfaces.ICredential
import com.pqixing.interfaces.ILog
import org.eclipse.jgit.api.Git
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
