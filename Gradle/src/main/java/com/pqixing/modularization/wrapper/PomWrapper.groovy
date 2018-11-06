package com.pqixing.modularization.wrapper

import com.pqixing.modularization.Keys
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.net.Net
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.MavenUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.tools.TextUtils

/**
 * Pom解析包裹类
 * http://192.168.3.7:9527/nexus/content/repositories/androidtest/com/dachen/android/dccommon/1.0.2/dccommon-1.0.2.pom
 */
class PomWrapper extends XmlWrapper {

    /**
     * 获取模块在仓库的meta url
     * @param envUrl
     * @param group
     * @param moduleName
     * @return
     * http://192.168.3.7:9527/nexus/content/repositories/androidtest/com/dachen/android/router/0.1.7/router-0.1.7.pom
     */
    static String getPomUrl(String envUrl, String group, String moduleName, String version) {
        return TextUtils.removeLineAndMark("$envUrl/${TextUtils.getUrl(group)}/$moduleName/$version/${moduleName}-${version}.pom")
    }


    static PomWrapper create(String envUrl, String group, String moduleName, String version) {
        String pomXml = MavenUtils.getDocPomXml(MavenUtils.getNameByUrl(envUrl), moduleName, version)
        if (!CheckUtils.isEmpty(pomXml)) return new PomWrapper(pomXml)
        return create(getPomUrl(envUrl, group, moduleName, version))
    }

    public static PomWrapper create(String url) {
        url = url.replaceAll("\r|\n", "")
        return new PomWrapper(Net.get(url, true))
    }

    public PomWrapper(String xmlString) {
        super(xmlString)
    }

    public String getGroupId() {
        return node.groupId.text()
    }

    public String getArtifactId() {
        return node.artifactId.text()
    }

    public String getVersion() {
        return node.version.text()
    }

    public String getPackaging() {
        return node.packaging.text()
    }

    public long getUpdateTime() {
        try {
            return node.name.text().split(Keys.SEPERATOR)[0].toLong()
        } catch (Exception e) {
            Print.lne(e)
        }
        return 0L
    }
    /**
     * 获取最后一次的git版本号
     * @return
     */
    public String getRevisionNum() {
        try {
            String name = node.name.text()
            return name.substring(name.indexOf(Keys.SEPERATOR) + Keys.SEPERATOR.length(), name.lastIndexOf(Keys.SEPERATOR))
        } catch (Exception e) {
            Print.lne(e)
        }
        return "null"
    }

    public String getGitLog() {
        try {
            String name = node.name.text()
            return name.substring(name.lastIndexOf(Keys.SEPERATOR) + Keys.SEPERATOR.length())
        } catch (Exception e) {
            Print.lne(e)
        }
        return "-----------"
    }

    com.pqixing.modularization.dependent.Module loadModule(com.pqixing.modularization.dependent.Module m) {
        m.artifactId = artifactId
        m.groupId = groupId
        m.version = version
        m.updateTime = updateTime
        m.gitLog = gitLog
        node.dependencies.dependency.each { d ->
            String groupId = d.groupId.text()
            String artifactId = d.artifactId.text()
            //不是内部依赖，则不处理
            if (groupId != GlobalConfig.groupName || artifactId == "dcannotation") return
            com.pqixing.modularization.dependent.Module dm = new com.pqixing.modularization.dependent.Module()
            dm.artifactId = d.artifactId.text()
            dm.groupId = d.groupId.text()
            dm.version = d.version.text()
            dm.scope = d.scope.text()
            m.modules.add(dm)
            d.exclusions.exclusion.each { e ->
                dm.exclude(group: e.groupId.text(), module: e.artifactId.text())
            }
        }
        return m
    }
    /**
     * 获取需要去除master依赖
     * @return
     */
    Set<String> getMasterExclude() {
        Set<String> excludes = new HashSet<>()
        node.dependencies.dependency.each { d ->
            d.exclusions.exclusion.each { e ->
                if (e.groupId.text() == Keys.GROUP_MASTER) {
                    e.artifactId.text().toString().split(Keys.SEPERATOR).each {
                        excludes.add(it)
                    }
                }
            }
        }
        return excludes
    }

}