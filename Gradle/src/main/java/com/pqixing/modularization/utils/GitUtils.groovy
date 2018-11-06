package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.git.GitConfig

import java.util.concurrent.TimeUnit

class GitUtils {
    static String run(String cmd, File dir,boolean printLog = true) {
        String result = ""

        try {
            def process = cmd.execute(null, dir)
            if (process == null) return ""

            if (process.waitFor(30, TimeUnit.SECONDS)) {
                InputStream input = process.exitValue() == 0 ? process.inputStream : process.errorStream;
                result = input?.getText(Keys.CHARSET)
                try {
                    process.closeStreams()
                    process.destroy()
                } catch (Exception e) {
                }
            } else {
                result = "Time Out count :1 MINUTES"
                process.waitForOrKill(1000 * 60)//1分钟后结束
            }
        } catch (Exception e) {
            Print.lne("GitUtils run ", e)
        }
        if(printLog) Print.ln("run: $cmd in $dir.name   ->   $result")
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
        if (!gitUrl.endsWith(".manager")) gitUrl += ".manager"
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
            File gitDir = new File(dir, ".manager")
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
        if(url == null) return ""
        return url.substring(url.lastIndexOf("/") + 1).replace(".manager", "").trim()
    }
}