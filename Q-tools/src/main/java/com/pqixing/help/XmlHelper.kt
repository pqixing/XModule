package com.pqixing.help

import com.pqixing.model.ProjectModel
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.SubModule
import com.pqixing.model.SubModuleType
import com.pqixing.tools.CheckUtils
import groovy.util.Node
import groovy.util.NodeList
import groovy.util.XmlParser
import groovy.xml.QName
import java.io.File

object XmlHelper {

    /**
     * 解析出pom文件的all exclude依赖
     */
    fun parsePomEclude(pomText: String, matchGroup: String): MavenPom {
        val pom = MavenPom()
        val node = XmlParser().parseText(pomText)

        pom.groupId = getChildNodeValue(node, "groupId")
        pom.artifactId = getChildNodeValue(node, "artifactId")
        pom.version = getChildNodeValue(node, "version")
        pom.packaging = getChildNodeValue(node, "packaging")
        pom.name = getChildNodeValue(node, "name")
        val dependencies = getChildNode(node, "dependencies")

        var first = true
        for (it in getChildNodeList(dependencies, "dependency")) {
            val dependency = it as? Node ?: continue
            val gid = getChildNodeValue(dependency, "groupId")
            if (gid.startsWith(matchGroup)) {
                pom.dependency.add(pairToStr(gid, getChildNodeValue(dependency, "artifactId")))
            }
            if (!first && pom.allExclude.isEmpty()) continue

            val exclude = HashSet<String>()
            val exclusions = getChildNodeList(dependency, "exclusions")
            if (exclusions.isNotEmpty()) getChildNodeList(exclusions[0] as Node, "exclusion").apply {
                if (isEmpty()) pom.allExclude.clear()
            }.forEach {
                val e = it as? Node ?: return@forEach
                val g = getChildNodeValue(e, "groupId")
                if (g.startsWith(matchGroup)) {
                    val p = "$g,${getChildNodeValue(e, "artifactId")}"
                    if (first) pom.allExclude.add(p)
                    else exclude.add(p)
                }
            }
            if (!first) pom.allExclude.removeIf { !exclude.contains(it) }
            first = false
        }
        return pom
    }


    fun parseMetadata(txt: String): MavenMetadata {
        val mate = MavenMetadata()
        val node = XmlParser().parseText(txt)

        mate.groupId = getChildNodeValue(node, "groupId")
        mate.artifactId = getChildNodeValue(node, "artifactId")
        val versioning = getChildNode(node, "versioning")
        mate.release = getChildNodeValue(versioning, "release")
        val versions = getChildNode(versioning, "versions")
        getChildNodeList(versions, "version").forEach {
            val v = it as? Node ?: return@forEach
            mate.versions.add(getNodeValue(v))
        }
        return mate
    }

    private fun getChildNode(paren: Node, name: String) = getChildNodeList(paren, name)[0] as Node
    private fun getChildNodeList(paren: Node, name: String): NodeList {
        val at = paren.getAt(QName(name))
        if (at.size > 0) return at
        return paren.get(name) as NodeList
    }

    private fun getChildNodeValue(paren: Node, name: String) = getNodeValue(getChildNode(paren, name))
    private fun getNodeValue(node: Node): String {
        val value = node.value().toString();
        return value.substring(1, value.length - 1)
    }

    @JvmStatic
    fun parseProjectXml(txt: File): ProjectXmlModel {

        val node = XmlParser().parseText(txt.readText())
        val baseUrl = node.get("@baseUrl").toString()
        val xmlModel = ProjectXmlModel(baseUrl)
        node.getAt(QName("project")).forEach {
            val p: Node = it as? Node ?: return@forEach

            val rootName = p.get("@name").toString()
            var introduce = p.get("@introduce").toString()
            val type = p.get("@type")?.toString() ?: SubModuleType.TYPE_LIBRARY

            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString() ?: ""
            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "$baseUrl/$rootName.git"
            val project = ProjectModel(rootName, introduce, gitUrl)
            xmlModel.projects.add(project)

            val children = p.getAt(QName("submodule"))
            if (children.isEmpty()) {
                project.addSubModule(SubModule(project, rootName, introduce, rootName, type))
            } else children.forEach { c ->
                if (c is Node) {
                    val name = c.get("@name").toString()
                    val t = c.get("@type")?.toString() ?: SubModuleType.TYPE_LIBRARY
                    introduce = c.get("@introduce").toString()
                    project.addSubModule(SubModule(project, name, introduce, "$rootName/$name", t))
                }
            }
        }

        return xmlModel
    }

    /**
     *
     */
    fun pairToStr(p: Pair<String?, String?>): String = "${p.first},${p.second}"

    fun pairToStr(first: String?, second: String): String = "$first,$second"

    fun strToPair(s: String): Pair<String?, String?> {
        val i = s.indexOf(",")
        if (i < 0) return Pair(s, null)
        return Pair(s.substring(0, i), s.substring(i + 1))
    }
}
