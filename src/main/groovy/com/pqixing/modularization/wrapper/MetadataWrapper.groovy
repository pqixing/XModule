package com.pqixing.modularization.wrapper

import com.pqixing.modularization.net.Net

/**
 * 头信息包裹类
 */
class MetadataWrapper extends XmlWrapper {
    /**
     * 获取模块在仓库的meta url
     * @param envUrl
     * @param group
     * @param moduleName
     * @return
     */
    static String getMetaUrl(String envUrl, String group, String moduleName) {
        return "$envUrl/${group.replace(".", "/")}/$moduleName/maven-metadata.xml"
    }

    public static MetadataWrapper create(String envUrl, String group, String moduleName) {
        return create(getMetaUrl(envUrl, group, moduleName))
    }

    public static MetadataWrapper create(String url) {
        return new MetadataWrapper(Net.get(url))
    }

    public MetadataWrapper(String xmlString) {
        super(xmlString)
    }

    public String getGroupId() {
        return node.groupId.text()
    }

    public String getArtifactId() {
        return node.artifactId.text()
    }

    public String getRelease() {
        return node.versioning.release.text()
    }

    public List<String> getVersions() {
        LinkedList<String> vs = new LinkedList<>()
        node.versioning.versions.version.each { vs += it.text() }
        return vs
    }

    public String getLastUpdated() {
        return node.versioning.lastUpdated.text()
    }
}