package com.pqixing.help

import com.pqixing.git.Components
import com.pqixing.tools.CheckUtils
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.QName
import java.io.File
import java.util.*
import kotlin.collections.HashSet

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
        pom.name = getChildNodeValue(node, "packaging")
        val dependencies = getChildNode(node, "dependencies")

        var first = true
        for (it in dependencies.getAt(QName("dependency"))) {
            val dependency = it as? Node ?: continue
            val gid = getChildNodeValue(dependency, "groupId")
            if (gid.startsWith(matchGroup)) {
                pom.dependency.add(pairToStr(gid, getChildNodeValue(dependency, "artifactId")))
            }
            if (!first && pom.allExclude.isEmpty()) continue

            val exclude = HashSet<String>()
            val exclusion = getChildNode(dependency, "exclusions").getAt(QName("exclusion"))
            if (exclusion.isEmpty()) pom.allExclude.clear()
            else exclusion.forEach {
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
