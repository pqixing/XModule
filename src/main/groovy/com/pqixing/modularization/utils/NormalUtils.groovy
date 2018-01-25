package com.pqixing.modularization.utils

import com.pqixing.modularization.Default
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * Created by pqixing on 17-12-8.
 */

class NormalUtils {

    /**
     * 判空处理
     * @param obj
     * @return
     */
    static boolean isEmpty(def obj) {
        if (null == obj || "" == obj.toString() || "null" == obj.toString()) return true
        if (obj instanceof Collection) return obj.isEmpty()
        return false
    }

    /**
     * 替换字符串中的变量
     * @param source
     * @return
     */
    static String parseString(String source, Map<String, Object> properties) {
        properties = properties.findAll { map -> !map.key.endsWith("Txt") }
        def builder = new StringBuilder()
        source.eachLine { str ->
            if (NormalUtils.isEmpty(str)) return

            boolean ignore = false
            Default.keyPattern.matcher(str).findAll()?.each { key ->
                if (ignore) return

                def value = properties.find { it.key == (findRealKey(key)) }?.value

                ignore = isEmpty(value) && key.startsWith("#1")

                if (!ignore) str = str.replace(key, String.valueOf(value))
            }
            if (!ignore) builder.append(str).append("\n")//替换#（任意）key
        }
        return builder.toString()
    }
    /**
     * 查找出待替换的key
     * @param source
     * @return
     */
    static String findRealKey(String key) {
        return key.substring(key.indexOf("{") + 1, key.lastIndexOf("}"))
    }

    /**
     * 解析最后的版本号
     * @param url
     * @return
     */
    static String parseLastVersion(String url) {
        return parseXmlByKey(request(url), "release")
    }
    /**
     * 集合转字符串
     * @param collection
     * @return
     */
    static String collection2Str(Collection collection) {
        if (isEmpty(collection)) return ""
        StringBuilder sb = new StringBuilder()
        collection.each { sb.append(it).append(",") }
        return sb.substring(0, sb.length() - 1)
    }

    static String request(String url) {
        try {
            return new URL(url).openStream().text
        } catch (Exception e) {
            ""
        }
    }
    /**
     * 获取模块在仓库的meta url
     * @param envUrl
     * @param group
     * @param moduleName
     * @return
     */
    static String getMetaUrl(String envUrl, String group, String moduleName) {
        StringBuilder url = new StringBuilder(envUrl)
        if (!envUrl.endsWith("/")) url.append("/")
        return url.append(group.replace(".", "/"))
                .append("/android/").append(moduleName).append("/maven-metadata.xml").toString()
    }
    /**
     * 获取模块在仓库的meta url
     * @param envUrl
     * @param group
     * @param moduleName
     * @return
     * http://192.168.3.7:9527/nexus/content/repositories/androidtest/com/dachen/android/router/0.1.7/router-0.1.7.pom
     */
    static String getPomUrl(String envUrl, String group, String moduleName, String version) {
        StringBuilder url = new StringBuilder(envUrl)
        if (!envUrl.endsWith("/")) url.append("/")
        return url.append(group.replace(".", "/"))
                .append("/android/").append(moduleName).append("/$version").append("/${moduleName}-${version}.pom").toString()
    }

    static List<String> parseListXmlByKey(String xmlTxt, String key) {
        LinkedList<String> result = []
        xmlTxt.findAll(Pattern.compile("<${key}>(?s).*?</${key}>")).each { r ->
            result += r.substring(key.length() + 2, r.length() - key.length() - 3)
        }
        return result
    }

    static String parseXmlByKey(String xmlTxt, String key) {
        String r = xmlTxt.find(Pattern.compile("<${key}>(?s).*?</${key}>"))
        if (isEmpty(r)) return ""
        return r.substring(key.length() + 2, r.length() - key.length() - 3)
    }
    /**
     * 获取当前分支名称
     * @param project
     * @return
     */
    static String getBranchName(Project project) {
        return "git rev-parse --abbrev-ref HEAD".execute(null, project.projectDir).text.trim()
    }

    static String getProperties(Project project, String key) {
        try {
            return project.ext.get(key)
        } catch (Exception e) {
            return ""
        }
    }
    /**
     * 获取最后提交记录
     * @param project
     * @return
     */
    static String getLastCommit(Project project) {
        String curBrach = ""
        " git branch -v".execute(null, project.projectDir).text.eachLine { line ->
            if (line.startsWith("*")) curBrach = line
        }
        return curBrach
    }

    static String getNameForBranch(Project project, String sourceName) {
        if (project.branchName == "master") return sourceName
        return "$sourceName-b-${project.branchName}"
    }
    /**
     * 获取最后的base版本号
     * @param project
     * @return
     */
    static String parseLastBaseVersion(Project project){
        String url = NormalUtils.getMetaUrl(project.moduleConfig.mavenType.maven_url, Default.groupName, NormalUtils.getNameForBranch(project,project.name))
        String lastVersion = NormalUtils.parseLastVersion(url)
        if(NormalUtils.isEmpty(lastVersion)) return ""
        return lastVersion.substring(0,lastVersion.lastIndexOf("."))
    }
}
