package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

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
        if (project.hasProperty("allRepo")) modules += new GroovyShell().evaluate(project.ext.get("defaultImpl"))
    }

    String getUrl(String moduleName) {
        StringBuilder url = new StringBuilder(mavenUrl)
        if (!mavenUrl.endsWith("/")) url.append("/")
        return url.append(compileGroup.replace(".", "/"))
                .append("/android/").append(moduleName).append("/maven-metadata.xml").toString()
    }

    String getBody(String url, OkHttpClient client) {
        try {
            return client.newCall(new Request.Builder()
                    .url(url)
                    .build()).execute()
                    .body().string();
        } catch (Exception e) {
            return ""
        }
    }

    @TaskAction
    void makeUrls() {
        urls = new HashMap<>()
        modules.each {
            def moduleName = it.replace(":", "")
            urls.put(moduleName, getUrl(moduleName))
        }
        println("urls : $urls")
    }

    @TaskAction
    void updateVersions() {
        def outFile = new File(outPath)
        outFile.parentFile.mkdirs()
        if (!outFile.exists()) outFile.createNewFile()

        def xmlFiles = new File(outFile.parentFile, ".xmls")

        def pros = new Properties()
        pros.load(outFile.newInputStream())
//        Long lastTime = pros.getProperty("lastUpdateTime")?.toLong()
        //1分钟内不重新加载
//        if (lastTime != null && System.currentTimeMillis() - lastTime > 1000 * 60) return

        OkHttpClient client = new OkHttpClient()
        urls.each { map ->
            String xmlString = getBody(map.value, client)
            String targetStr = xmlString.find(Pattern.compile("<release>(?s).*?</release>"))
            if (!NormalUtils.isEmpty(targetStr)) {
                pros.put(map.key, targetStr.substring(9, targetStr.lastIndexOf("</release>")))
            }
        }
        pros.put("lastUpdateTime", System.currentTimeMillis())
        pros.store(outFile.newOutputStream(), "")
    }
}
