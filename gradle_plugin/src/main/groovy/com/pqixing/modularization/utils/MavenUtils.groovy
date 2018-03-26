package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.net.Net
import com.pqixing.modularization.wrapper.MetadataWrapper
import com.pqixing.modularization.wrapper.PomWrapper
import com.pqixing.modularization.wrapper.ProjectWrapper

class MavenUtils {
    static String getNameByUrl(String mavenUrl) {
        String name = Keys.TEST
        GlobalConfig.preMavenUrl.each { map ->
            if (map.value == mavenUrl) name = map.key
        }
        return name
    }

    static String getDocMetaXml(String mavenName, String artifactId) {
        return FileUtils.read(new File(documentDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName/$artifactId/meta.xml"))
    }

    static String getDocPomXml(String mavenName, String artifactId, String version) {
        return FileUtils.read(new File(documentDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName/$artifactId/$version/pom.xml"))
    }

    /**
     * 获取最后的版本号
     * @param mavenName
     * @param artifactId
     * @return
     */
    static String getVersion(String mavenName, String artifactId) {
        return getMavenMaps(mavenName).get(artifactId)?.toString() ?: "+"
    }

    static Properties getMavenMaps(String mavenName) {
        String mapKey = "${mavenName}Maps"
        if (BasePlugin.rootProject.hasProperty(mapKey)) {
            return BasePlugin.rootProject."$mapKey"
        }
        long start = System.currentTimeMillis()
        //如果当前不存在版本号，则从遍历Doc目录创建
        def maps = new Properties()
        def mapsDir = new File(GlobalConfig.updateBeforeSync?upDocumentDir:documentDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName/")
        mapsDir.eachDir { dir ->
            String version = ""
            dir.eachDir {
                String newVersion = it.name.trim()
                int len = newVersion.length() - version.length()
                if (len == 0 && newVersion > version || len > 0) version = newVersion
            }
            maps.put(dir.name, version)
        }

        BasePlugin.rootProject.ext."$mapKey" = maps
//        Print.lnf("getMavenMaps cout:${System.currentTimeMillis() - start} -> $maps")
//        FileUtils.createIfNotExist(mapFile)
        return BasePlugin.rootProject."$mapKey"
    }


    static File getDocumentDir() {
        String docGitName = GitUtils.getNameFromUrl(GlobalConfig.docGitUrl)
        return GitUtils.findGitDir(new File(BasePlugin.rootProject.rootDir.parentFile, docGitName))
    }

    static File getUpDocumentDir() {
        File docDir = documentDir
        if (CheckUtils.isEmpty(docDir)) {//如果文档库还不存在
            GitUtils.run("git clone $GlobalConfig.docGitUrl", docDir.parentFile)
        } else GitUtils.run("git pull", docDir)

        return docDir
    }

//    static boolean clearVersionMaps(String mavenName) {
//        File docDir = getUpDocumentDir()
//        if (!docDir.exists()) return false
//
//        File mavenDir = new File(docDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName")
//
//        //保存最新的版本号
//        new File(mavenDir, Keys.FILE_VERSION).delete()
//    }

    /**
     * 更新某个模块的maven仓库记录
     */
    static boolean updateMavenRecord(ProjectWrapper wrapper, String mavenName, String mavenUrl, String artifactId, boolean push = true) {
        MetadataWrapper metaWrapper = MetadataWrapper.create(mavenUrl, GlobalConfig.groupName, artifactId)
        if (metaWrapper.empty) return false

        File docDir = push ? upDocumentDir : documentDir;
        if (!docDir.exists()) return false

        File mavenDir = new File(docDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName")

        //保存最新的版本号
        getMavenMaps(mavenName).put(artifactId, metaWrapper.release)

        //缓存meta 文件
        FileUtils.write(new File(mavenDir, "$artifactId/meta.xml"), metaWrapper.xmlString)
        def versions = metaWrapper.versions
        Print.ln("updateMavenRecord $mavenName $artifactId:- >$versions")
        //缓存所有版本的pom 文件
        versions.each { v ->
            File pomFile = new File(mavenDir, "$artifactId/$v/pom.xml")
            if (!pomFile.exists()) {
                String pomXml = Net.get(PomWrapper.getPomUrl(mavenUrl, GlobalConfig.groupName, artifactId, v), true)
                if (!CheckUtils.isEmpty(pomXml)) FileUtils.write(pomFile, pomXml)
            }
        }
        if (push) pushMaven()
        return true
    }
    /**
     * push文件库
     */
    static void pushMaven() {
        File docDir = getUpDocumentDir()
        GitUtils.run("git add $Keys.MODURIZATION/*", docDir)
        GitUtils.run("git commit -m update", docDir)
        GitUtils.run("git push", docDir)
    }
}