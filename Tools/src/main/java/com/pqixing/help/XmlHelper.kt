package com.pqixing.help

import com.pqixing.git.GitProject
import com.pqixing.tools.CheckUtils
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.QName
import java.io.File
import java.util.*

object XmlHelper {

    fun parseMetadata(txt: String, mate: MavenMetadata) {
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
    }

    private fun getChildNode(paren: Node, name: String) = (paren.getAt(QName(name))[0] as Node)
    private fun getChildNodeValue(paren: Node, name: String) = getNodeValue(getChildNode(paren, name))
    private fun getNodeValue(node: Node): String {
        val value = node.value().toString();
        return value.substring(1, value.length - 1)
    }

    @JvmStatic
    fun parseProjectXml(txt: File, projects: HashMap<String, GitProject>) {
        val node = XmlParser().parseText(txt.readText())
        val baseUrl = node.get("@baseUrl").toString()
        node.getAt(QName("project")).forEach {
            val p: Node = it as? Node ?: return@forEach

            val rootName = p.get("@name").toString()
            var introduce = p.get("@introduce").toString()

            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString()?:""
            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "$baseUrl/${rootName}.git"

            val children = p.getAt(QName("submodule"))
            if (children.isEmpty()) {
                addProject(projects, rootName, gitUrl, introduce, rootName)
            } else children.forEach { c ->
                val cp = c as? Node ?: return@forEach
                val name = cp.get("@name").toString()
                introduce = cp.get("@introduce").toString()
                addProject(projects, name, gitUrl, introduce, rootName)
            }
        }

    }

    private fun addProject(projects: HashMap<String, GitProject>, name: String, gitUrl: String, introduce: String, rootName: String) {
        val project = GitProject(name, gitUrl, introduce, rootName)
        projects[name] = project
    }
}
