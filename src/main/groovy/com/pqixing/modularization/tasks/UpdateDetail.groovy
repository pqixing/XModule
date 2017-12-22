package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.pqixing.modularization.utils.FileUtils

/**
 * Created by pqixing on 17-12-22.
 */

public class UpdateDetail extends DefaultTask {
    String moduleName
    Map<String, String> envs
    String compileGroup

    UpdateDetail() {
        group = Default.taskGroup
    }

    @TaskAction
    void run() {
        envs.each { map -> generatorLogFile(map.key, map.value) }
    }
    /**
     * 生成md文件
     * @param env
     * @param urls
     */
    void generatorLogFile(String env, String urls) {
        StringBuilder sb = new StringBuilder("##  ${moduleName}组件更新日志   \n")
        String metaUrl = NormalUtils.getMetaUrl(urls, compileGroup, moduleName)
        List<String> versions = NormalUtils.parseListXmlByKey(NormalUtils.request(metaUrl), 'version')
        if (NormalUtils.isEmpty(versions)) return
        versions.sort { it }.each { version ->
            String pomUrls = NormalUtils.getPomUrl(urls, compileGroup, moduleName, version)
            String name = NormalUtils.parseXmlByKey(NormalUtils.request(pomUrls), "name")
            sb.append("\n###     ${version}    \n")
            int splitIndex = name.indexOf("##")
            String updateDesc = name
            if (splitIndex > 0) {
                try {
                    sb.append("更新时间:${new Date(name.substring(0, splitIndex).toLong()).toLocaleString()}   \n")
                } catch (Exception e) {
                }
                updateDesc = name.substring(splitIndex + 2, name.length())
            }
            sb.append("\n\n   依赖方式:implementation 'com.dachen.android:router:$version'　　　　　　[下载](${pomUrls.replace(".pom", ".aar")})    \n")
            sb.append(" > 更新说明:$updateDesc  ")
            sb.append("\n --- \n")
        }

        FileUtils.write(new File(project.buildDir, "${env}/${moduleName}.md"), sb.toString())
    }
}
