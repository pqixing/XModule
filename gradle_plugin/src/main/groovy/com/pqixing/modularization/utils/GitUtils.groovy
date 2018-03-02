package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.git.GitConfig

class GitUtils {
    static String run(String cmd, File dir) {
        String result = ""
        try {
            def process = cmd.execute(null, dir)
            if (process == null) return ""
            InputStream input = process.waitFor() == 0 ? process.inputStream : process.errorStream;
            result = input?.getText(Keys.CHARSET)

            process.closeStreams()
            if (process.alive) process.destroy()
            Thread.sleep(2000)//两秒后关闭

        } catch (Exception e) {
            Print.lne("GitUtils run ",e)
        }
        return result
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