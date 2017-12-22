package com.pqixing.modularization.tasks

import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-22.
 */

public class UpdateDetail extends DefaultTask {
    File logPath
    String moduleName
    Map<String, String> envs
    String compileGroup

    @TaskAction
    void run() {
        new File(logPath, "build/${env}/${moduleName}.md)")
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

        def size = versions.size()
        for (int i = size - 1; i >= 0; i--) {
        }

//        FileUtils.write(new File(logPath, "${env}_log.md"), sb.toString())
    }
    /**
     * 检测是否存在详细页面
     * @param env
     * @param module
     * @return
     */
    boolean checkHasDetailFile(String env, String moduleName) {
        return new File(logPath, "build/${env}/${moduleName}.md)").exists()
    }
}
