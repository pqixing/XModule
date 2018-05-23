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
     * 输出保存的文件
     * @param mavenName
     * @param branchName
     * @param outFile
     */
    static void saveMavenMaps(String mavenName, String branchName, String filterBranch = "all", File outFile = getFocusMapsFile(mavenName, branchName)) {
        String result = ""
        if (outFile.exists()) {
            String sourcePath = outFile.absolutePath
            def targetFile = new File(outFile.getParent(), outFile.name + ".bak")
            outFile.renameTo(targetFile)
            result += "fileExist=$targetFile.absolutePath&"
            outFile = new File(sourcePath)
        }

        def maps = MavenUtils.getMavenMaps(mavenName)
        MavenUtils.getFocusMavenMaps(mavenName, branchName).each { maps.put(it.key, it.value) }

        if ("all" != filterBranch) {
            def m = maps
            maps = new Properties()
            def master = "master" == filterBranch
            m.each {
                def name = it.key.toString()
                if ((master && !name.contains(Keys.BRANCH_TAG)) || name.contains(filterBranch)) {
                    maps.put(name, it.value)
                }
            }
        }

        FileUtils.saveMaps(maps, outFile)
        result += "file=$outFile.absolutePath"
        Print.lnIde(result)
    }
    /**
     * 获取最后的版本号
     * @param mavenName
     * @param artifactId
     * @return
     */
    static String getVersion(String mavenName, String branchName, String artifactId) {
        return getFocusMavenMaps(mavenName, branchName)?.get(artifactId)?.toString() ?: (getMavenMaps(mavenName)?.get(artifactId)?.toString() ?: "+")
    }
    /**
     * 获取制定分支的focusMaven配置
     * @param mavenName
     * @param branchName
     * @return
     */
    static File getFocusMapsFile(String mavenName, String branchName) {
        return new File(documentDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName/$Keys.DIR_VERSIONS/${branchName}.version")
    }
    /**
     * 获取指定版本号
     * @param mavenName
     * @param branchName
     * @return
     */
    static Properties getFocusMavenMaps(String mavenName, String branchName) {
        String mapKey = "$mavenName${branchName}Maps"
        if (BasePlugin.rootProject.hasProperty(mapKey)) {
            return BasePlugin.rootProject."$mapKey"
        }

        Properties properties = new Properties()
        //加载指定分支的版本
        def branchVersionFile = getFocusMapsFile(mavenName, branchName)
        if (branchVersionFile.exists()) properties.load(branchVersionFile.newInputStream())
        def focusVersionFile = new File(GlobalConfig.focusVersions)
        if (focusVersionFile.exists()) properties.load(focusVersionFile.newInputStream())
        BasePlugin.rootProject.ext."$mapKey" = properties
//        Print.ln("getFocusMavenMaps mavenName $mavenName :$branchName -> $properties")
        return properties
    }

    static Properties getMavenMaps(String mavenName) {
        String mapKey = "${mavenName}Maps"
        if (BasePlugin.rootProject.hasProperty(mapKey)) {
            Properties cacheMap = BasePlugin.rootProject."$mapKey"
            if(cacheMap!=null && !cacheMap.isEmpty()) return cacheMap
        }
//        long start = System.currentTimeMillis()
        //如果当前不存在版本号，则从遍历Doc目录创建
        def maps = new Properties()
        def mapsDir = new File(GlobalConfig.updateBeforeSync ? upDocumentDir : documentDir, "$Keys.MODURIZATION/$Keys.MAVEN/$mavenName/")
        if (mapsDir.exists()) mapsDir.eachDir { dir ->
            String version = ""
            def metaFile = new File(dir, "meta.xml")
            if (metaFile.exists()) {
                version = new MetadataWrapper(FileUtils.read(metaFile)).release
            } else dir.eachDir {
                String newVersion = it.name.trim()
                if (TextUtils.compareVersion(newVersion, version) > 0) version = newVersion
            }
            maps.put(dir.name, version)
        }
        BasePlugin.rootProject.ext."$mapKey" = maps
//        Print.ln("getMavenMaps mavenName $mavenName -> $maps")
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
//        def versions = metaWrapper.versions
//        Print.ln("updateMavenRecord $mavenName $artifactId:- >$versions")
//        //缓存所有版本的pom 文件
//        versions.each { v ->
//            File pomFile = new File(mavenDir, "$artifactId/$v/pom.xml")
//            if (!pomFile.exists()) {
//                String pomXml = Net.get(PomWrapper.getPomUrl(mavenUrl, GlobalConfig.groupName, artifactId, v), true)
//                if (!CheckUtils.isEmpty(pomXml)) FileUtils.write(pomFile, pomXml)
//            }
//        }
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