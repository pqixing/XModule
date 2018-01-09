package com.pqixing.modularization.utils

import org.gradle.api.Project

/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {
    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String appendUrls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append("/")
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    /**
     * 输出文件
     * @param file
     * @param data
     */
    static String write(File file, String data) {
        if (file.exists()) file.delete()
        file.parentFile.mkdirs()
        BufferedOutputStream out = file.newOutputStream()
        out.write(data.getBytes())
        out.flush()
        out.close()
        return file.path
    }

    /**
     * 输出依赖关系
     * @param project
     * @return
     */
    static void writeDependency(Project project, File outputFile) {
        def strList = new LinkedList<String>()
        outputFile.eachLine {
            if (it.startsWith("No dependencies")) {
                strList.removeLast()
                strList.removeLast()
            } else {
                strList.add(it + "\n")
            }
        }

        HashMap<String, Integer> moduleLevels = new HashMap<>()
        dependencyByLevel(project, moduleLevels, 1)
        def maps = moduleLevels.toSpreadMap().sort { it.value }
        StringBuilder mapSb = new StringBuilder("本地工程依赖层级关系: \n 0 -> $project.name")
        int curLevel = 0
        maps.each { map ->
            if (map.value > curLevel) mapSb.append("\n $map.value -> ")
            mapSb.append("$map.key  ")
            curLevel = map.value
        }
        mapSb.append("\n")

        FileUtils.write(outputFile, mapSb.toString())
        outputFile.append(strList.toString())
        if (project.hasProperty("hiddenConfig"))
            ["debug", "test", "release"].each { writePatchUpload(maps, project.buildDir, it) }

    }
    /**
     * 生成批量上传的脚本
     * @param maps
     * @param outDir
     * @param m
     */
    static void writePatchUpload(Map<String, Integer> maps, File outDir, String envName) {
        List<String> moduleNames = new LinkedList<>()
        maps.each { moduleNames.add(0, it.key) }

        StringBuilder sb = new StringBuilder("#!/usr/bin/env bash \n")
        moduleNames.each { name ->
            String taskName = "${name}Upload-$envName"
            sb.append("gradle :$name:$taskName  \n")
            sb.append("sleep 10s  \n")
        }
        write(new File(outDir, "upload${envName}.txt"), sb.toString())
    }

    static void dependencyByLevel(Project project, HashMap<String, Integer> moduleLevels, int curLevel) {
        if (project == null || !project.hasProperty("moduleConfig")) return
        List<String> modulesName = project.moduleConfig.dependModules.moduleNames
        modulesName.each { moduleLevels.put(it, curLevel) }
        modulesName.each { name ->
            dependencyByLevel(project.rootProject.allprojects.find {
                it.name == name
            }, moduleLevels, curLevel + 1)
        }
    }

}
