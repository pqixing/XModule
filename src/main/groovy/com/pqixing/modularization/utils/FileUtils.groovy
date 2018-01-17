package com.pqixing.modularization.utils

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.MavenType
import org.gradle.api.Project

/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {
    /**
     * 读取缓存的pom依赖信息
     * @param maven
     * @param model
     * @return
     */
    static String readCachePom(MavenType maven, String name, String version) {
        File pomFile = new File(maven.project.rootDir, ".modularization/poms_$maven.name/${name}-${version}.pom")
        if (pomFile.exists()) return pomFile.text
        String pomStr = NormalUtils.request(NormalUtils.getPomUrl(maven.maven_url, Default.groupName, name, version))
        if (!NormalUtils.isEmpty(pomStr)) write(pomFile, pomStr)
    }

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
        StringBuilder mapSb = new StringBuilder("-\n")
        if(project.hasProperty("focusLocal")&&"Y" == project.ext.get("focusLocal")) {
            HashMap<String, Integer> moduleLevels = new HashMap<>()
            dependencyByLevel(project, moduleLevels, 1)
            def maps = moduleLevels.toSpreadMap().sort { it.value }
            mapSb.append("本地工程依赖层级关系: \n 0 -> $project.name")
            int curLevel = 0
            maps.each { map ->
                if (map.value > curLevel) mapSb.append("\n $map.value -> ")
                mapSb.append("$map.key  ")
                curLevel = map.value
            }
            mapSb.append("\n")
            if (project.hasProperty("hiddenConfig"))
                ["debug", "test", "release"].each { writePatchUpload(maps, project.buildDir, it) }
        }
        FileUtils.write(outputFile, mapSb.toString())
        outputFile.append(strList.toString())


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
            sb.append('''echo "modules+=':#{s1}'" > config2.gradle  \n'''.replace("#{s1}", name))
            sb.append("gradle :$name:clean  \n")
            sb.append("gradle :$name:$taskName  \n")
            sb.append("sleep 10s  \n")
        }
        write(new File(outDir, "upload${envName}.txt"), sb.toString())
    }

    static void dependencyByLevel(Project project, HashMap<String, Integer> moduleLevels, int curLevel) {
        if (curLevel > 10 || project == null || !project.hasProperty("moduleConfig")) return
        List<String> modulesName = project.moduleConfig.dependModules.moduleNames
        modulesName.each { moduleLevels.put(it, Math.max(moduleLevels.get(it) ?: 0, curLevel)) }
        modulesName.each { name ->
            dependencyByLevel(project.rootProject.allprojects.find {
                it.name == name
            }, moduleLevels, curLevel + 1)
        }
    }

}
