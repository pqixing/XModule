package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.git.GitConfig

class GitUtils {
    static String run(String cmd, File dir) {
        return cmd.execute(null, dir)?.in?.getText(Keys.CHARSET)
    }
    /**
     * 获取完整的giturl
     * @param username
     * @param password
     * @param gitUrl
     * @return
     */
    static String getFullGitUrl(String gitUrl) {
        if (!gitUrl.endsWith(".git")) gitUrl += ".git"
        if (!gitUrl.contains("@")) gitUrl = gitUrl.replace("//", "//$GitConfig.userName:$GitConfig.password@")
        return gitUrl
    }
    /**
     * 查找出对应的git根目录
     * @param dir
     * @return
     */
    static File findGitDir(File dir) {
        while (dir != null) {
            File gitDir = new File(dir, ".git")
            if (gitDir.exists() && gitDir.isDirectory()) return dir
            dir = dir.parentFile
        }
        return null
    }
    /**
     * 获取git名称
     * @param url
     * @return
     */
    static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1).replace(".git", "")
    }
}