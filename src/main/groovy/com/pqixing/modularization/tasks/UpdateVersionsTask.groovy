package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.text.SimpleDateFormat

/**
 * Created by pqixing on 17-12-18.
 */

public class UpdateVersionsTask extends DefaultTask {
    String outPath
    String mavenUrl
    String compileGroup
    HashSet<String> modules
    HashMap<String, String> urls
    //自动搜索模块
    boolean searchModules = true

    private static String updateTimeFormat = "yyyy-MM-dd-HH-mm-ss"
    SimpleDateFormat format

    UpdateVersionsTask() {
        group = Default.taskGroup
        modules = new HashSet<>()
        modules += Default.allRepo
        format = new SimpleDateFormat(updateTimeFormat)
    }

    @TaskAction
    void run() {
        searchModulesByPath()
        makeUrls()
        updateVersions()
    }

    void searchModulesByPath() {
        if (!searchModules) return
        modules += getModuleNames(project.rootDir.parentFile, 3)
        Print.ln("searchModules all modules :$modules")
    }

    /**
     * @param dir
     * @param deep 层级，如果小于0 停止获取
     * @return
     */
    HashSet<String> getModuleNames(File dir, int deep) {
        if (deep < 0) return []
        def set = []
        if (new File(dir, "build.gradle").exists() && new File(dir, "src").exists()) set += dir.name
        else {
            dir.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File file, String s) {
                    return file.isDirectory()
                }
            }).each { childDir -> set += getModuleNames(childDir, deep - 1) }
        }
        return set
    }


    void makeUrls() {
        urls = new HashMap<>()
        modules.each {
            def moduleName = it.replace(":", "")
            urls.put(moduleName, NormalUtils.getMetaUrl(mavenUrl, compileGroup, moduleName))
        }
        println("urls : $urls")
    }

    void updateVersions() {
        def outFile = new File(outPath)
        outFile.parentFile.mkdirs()
        def pros = new Properties()
        if (outFile.exists()) pros.load(outFile.newInputStream())

        urls.each { map ->
            String timeStr = "${map.key}-stamp"
            String timeStamp = "${map.key}-last"

            //10秒内,不更新相同的组件版本,避免不停的爬取相同的接口
            if (System.currentTimeMillis() - (pros.getProperty(timeStamp)?.toLong() ?: 0L) <= 1000 * 10) return

            String version = NormalUtils.parseLastVersion(map.value)
            if (!NormalUtils.isEmpty(version)) {
                if (version != (pros.getProperty(map.key) ?: ""))
                    pros.put(timeStr, format.format(new Date()))

                pros.put(map.key, version)
                pros.put(timeStamp, System.currentTimeMillis().toString())

            }
        }
//        pros.put("lastUpdateTime", System.currentTimeMillis().toString())
        pros.store(outFile.newOutputStream(), "")
    }
}
