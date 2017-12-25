package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-18.
 */

public class UpdateVersionsTask extends DefaultTask {
    String outPath
    String mavenUrl
    String compileGroup
    HashSet<String> modules
    HashMap<String, String> urls

    UpdateVersionsTask() {
        group = Default.taskGroup
        modules = new HashSet<>()
        modules += Default.allRepo
    }
    @TaskAction
    void run(){
        makeUrls()
        updateVersions()
    }


    void makeUrls() {
        urls = new HashMap<>()
        modules.each {
            def moduleName = it.replace(":", "")
            urls.put(moduleName, NormalUtils.getMetaUrl(mavenUrl,compileGroup,moduleName))
        }
        println("urls : $urls")
    }

    void updateVersions() {
        def outFile = new File(outPath)
        outFile.parentFile.mkdirs()
        if (!outFile.exists()) outFile.createNewFile()


        def pros = new Properties()
        pros.load(outFile.newInputStream())
        long diff = System.currentTimeMillis() - (pros.getProperty("lastUpdateTime")?.toLong() ?: 0L)
        //30秒内不重新加载
        if (diff <= 1000 * 30) return

        urls.each { map ->
            String version = NormalUtils.parseLastVersion(map.value)
            if (!NormalUtils.isEmpty(version)) {
                pros.put(map.key, version)
            }
        }
        pros.put("lastUpdateTime", System.currentTimeMillis().toString())
        pros.store(outFile.newOutputStream(), "")
    }
}
