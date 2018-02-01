package com.pqixing.modularization.dependent

import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-18.
 */

public class VersionsUpdateTask extends BaseTask
{

    VersionsUpdateTask() {
        group = Default.taskGroup
    }


    @TaskAction
    void run() {
        searchModulesByPath()
        makeUrls()
        updateVersions()
    }

    @Override
    void start() {

    }

    @Override
    void runTask() {

    }

    @Override
    void end() {

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
            if ("master" != project.branchName)
                urls.put(NormalUtils.getNameForBranch(project,moduleName), NormalUtils.getMetaUrl(mavenUrl, compileGroup, NormalUtils.getNameForBranch(project,moduleName)))
        }
        Print.ln("urls : $urls")
    }

    void updateVersions() {
        def outFile = new File(outPath)
        outFile.parentFile.mkdirs()
        def pros = new Properties()
        if (outFile.exists()) pros.load(outFile.newInputStream())

        urls.each { map ->
            String timeStr = "${map.key}-last"
            String timeStamp = "${map.key}-stamp"

            //10秒内,不更新相同的组件版本,避免不停的爬取相同的接口
            if (System.currentTimeMillis() - (pros.getProperty(timeStamp)?.toLong() ?: 0L) <= 1000 * 20) return

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
