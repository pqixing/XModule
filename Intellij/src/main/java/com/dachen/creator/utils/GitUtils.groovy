package com.dachen.creator.utils

import java.util.concurrent.TimeUnit

class GitUtils {


    static List<String> run(String cmd, File dir, Closure<String> eachLine = null) {
        List<String> result = []

        try {
            def process = cmd.execute(null, dir)
            if (process == null) return ""
//            if (process.waitFor(30, TimeUnit.SECONDS)) {
//                InputStream input = process.exitValue() == 0 ? process.inputStream : process.errorStream;
            InputStream input = process.inputStream
            try {
                input.eachLine("utf-8") { line ->
                    result += line
                    eachLine?.call(line)
                }
                process.closeStreams()
                process.destroy()
            } catch (Exception e) {
                result += "Exception ${e.toString()}"
            }
//            } else {
//                result += "Time Out count :1 MINUTES"
//                process.waitForOrKill(1000 * 60)//1分钟后结束
//            }
        } catch (Exception e) {
        }
        print("run:$cmd file:$dir result:$result")
        return result
    }
    /**
     * 查找所有的分支
     * @param dir
     * @return
     */
    static Set<String> findBranchs(String dir) {
        if (dir == null) return []
        def gitDir = GitUtils.findGitDir(new File(dir))
        if (gitDir == null) return []
        def set = new HashSet<String>()
        GitUtils.run("git branch -a", gitDir) {
            String l = it.replace("*", "")
            def i = l.lastIndexOf("/")
            if (i < 0) {
                set.add(l.trim())
            } else {
                set.add(l.substring(i + 1).trim())
            }
        }
        return set
    }

    static String findBranchName(String dir) {
        if (dir == null) return ""
        def gitDir = GitUtils.findGitDir(new File(dir))
        if (gitDir == null) return ""
        return GitUtils.run("git rev-parse --abbrev-ref HEAD", gitDir)?.last()
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