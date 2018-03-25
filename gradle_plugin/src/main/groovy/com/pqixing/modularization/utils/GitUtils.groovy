package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.git.GitConfig

import java.util.concurrent.TimeUnit

class GitUtils {
    static String run(String cmd, File dir) {
        String result = ""

        try {
            def process = cmd.execute(null, dir)
            if (process == null) return ""

            if (process.waitFor(2, TimeUnit.MINUTES)) {
                InputStream input = process.exitValue() == 0 ? process.inputStream : process.errorStream;
                result = input?.getText(Keys.CHARSET)
            } else {
                result = "run $cmd : Time Out count :2 MINUTES"
                Print.ln(result)
            }
            process.closeStreams()
            if (process.alive){
                process.destroy()
            }
            Thread.sleep(1000)//两秒后关闭

        } catch (Exception e) {
            Print.lne("GitUtils run ", e)
        }
        Print.ln("run: $cmd in $dir.name   ->   $result")
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