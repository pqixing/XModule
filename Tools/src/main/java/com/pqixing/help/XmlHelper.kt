package com.pqixing.help

import com.pqixing.git.Components
import com.pqixing.tools.CheckUtils
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.QName
import java.io.File
import java.util.*

object XmlHelper {

    fun parseMetadata(txt: String, groupUrl: String): MavenMetadata {
        val mate = MavenMetadata(groupUrl)
        val node = XmlParser().parseText(txt)
        mate.groupId = getChildNodeValue(node, "groupId")
        mate.artifactId = getChildNodeValue(node, "artifactId")
        val versioning = getChildNode(node, "versioning")
        mate.release = getChildNodeValue(versioning, "release")
        val versions = getChildNode(versioning, "versions")
        versions.getAt(QName("version")).forEach {
            val v = it as? Node ?: return@forEach
            mate.versions.add(getNodeValue(v))
        }
        return mate
    }

    private fun getChildNode(paren: Node, name: String) = (paren.getAt(QName(name))[0] as Node)
    private fun getChildNodeValue(paren: Node, name: String) = getNodeValue(getChildNode(paren, name))
    private fun getNodeValue(node: Node): String {
        val value = node.value().toString();
        return value.substring(1, value.length - 1)
    }

    @JvmStatic
    fun parseProjectXml(txt: File, projects: HashMap<String, Components>) {
        val node = XmlParser().parseText(txt.readText())
        val baseUrl = node.get("@baseUrl").toString()
        node.getAt(QName("project")).forEach {
            val p: Node = it as? Node ?: return@forEach

            val rootName = p.get("@name").toString()
            var introduce = p.get("@introduce").toString()
            var type = p.get("@type")?.toString() ?: Components.TYPE_LIBRARY

            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString() ?: ""
            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "$baseUrl/${rootName}.git"

            val children = p.getAt(QName("submodule"))
            if (children.isEmpty()) {
                addProject(projects, rootName, gitUrl, introduce, rootName, type)
            } else children.forEach { c ->
                val cp = c as? Node ?: return@forEach
                val name = cp.get("@name").toString()
                val type = p.get("@type")?.toString() ?: Components.TYPE_LIBRARY
                introduce = cp.get("@introduce").toString()
                addProject(projects, name, gitUrl, introduce, rootName, type)
            }
        }

    }

    private inline fun addProject(projects: HashMap<String, Components>, name: String, gitUrl: String, introduce: String, rootName: String, type: String) {
        val project = Components(name, gitUrl, introduce, rootName, type)
        projects[name] = project
    }
}
