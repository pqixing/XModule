package com.pqixing.git

import com.pqixing.Tools
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
    fun clone(gitUrl: String, dirPath: String) = clone(gitUrl, dirPath, "master", Tools.logger)

    @JvmStatic
    fun clone(gitUrl: String, dirPath: String, branchName: String) = clone(gitUrl, dirPath, branchName, Tools.logger)

    @JvmStatic
    fun clone(gitUrl: String, dirPath: String, logger: ILog) = clone(gitUrl, dirPath, "master", logger)

    /**
     * clone
     * @param gitUrl
     * @param dirPath
     * @return
     */
    @JvmStatic
    fun clone(gitUrl: String, dirPath: String, branchName: String, logger: ILog): Boolean {
        val name = getGitNameFromUrl(gitUrl)
        val gitDir = File(dirPath, name)
        return try {
            Git.cloneRepository()
                    .setCredentialsProvider(UsernamePasswordCredentialsProvider(credentials.getUserName(), credentials.getPassWord()))
                    .setURI(gitUrl).setDirectory(gitDir).setBranch(branchName)
                    .setProgressMonitor(PercentProgress(logger))
                    .call()
            isGitDir(gitDir)
        } catch (e: Exception) {
            logger?.println(e.toString())
            false
        }
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

    internal inline fun isGitDir(dir: File): Boolean {
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
