package com.pqixing.help

import com.pqixing.Tools
import com.pqixing.model.ProjectModel
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.SubModule
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
                pom.dependency.add("$gid,${getChildNodeValue(dependency, "artifactId")}")
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
        val xmlModel = ProjectXmlModel(node.get("@baseUrl")?.toString() ?: "")
        xmlModel.templetUrl = node.get("@templetUrl")?.toString() ?: ""
        xmlModel.mavenUrl = node.get("@mavenUrl")?.toString() ?: ""
        xmlModel.mavenGroup = node.get("@mavenGroup")?.toString() ?: ""
        xmlModel.mavenUser = node.get("@mavenUser")?.toString() ?: ""
        xmlModel.mavenPsw = node.get("@mavenPsw")?.toString() ?: ""
        xmlModel.createSrc = node.get("@createSrc") == "true"
        xmlModel.baseVersion = node.get("@baseVersion")?.toString()?:""
        xmlModel.matchingFallbacks.addAll ((node.get("@matchingFallbacks")?.toString()?:"").split(",").filter { it.isNotEmpty() })

        parseProjects(xmlModel, node, "")
        xmlModel.allSubModules().forEach {
            val api = it.apiModel ?: return@forEach
            if (api.path.isEmpty()) {
                it.apiModel = xmlModel.findSubModuleByName(api.name)
            }
        }
        node.localText()
        return xmlModel
    }

    private fun parseProjects(xmlModel: ProjectXmlModel, node: Node?, path: String) {
        node ?: return
        //解析分组
        node.getAt(QName("group"))?.forEach {
            val group: Node = it as? Node ?: return@forEach
            parseProjects(xmlModel, group, "$path/${group.get("@name")?.toString() ?: ""}")
        }

        node.getAt(QName("project")).forEach {
            val p: Node = it as? Node ?: return@forEach

            val name = p.get("@name").toString()
            var introduce = p.get("@introduce").toString()


            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString() ?: ""

            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "${xmlModel.baseUrl}/$name.git"

            val project = ProjectModel(name, "$path/$name", introduce, gitUrl)


            xmlModel.projects.add(project)

            //type不为空，本身也作为一个模块添加
            if (p.get("@type") != null) addNewSubModule(project, p, path)

            parseSubModule(project, p, path+"/"+p.get("@name")?.toString())
        }
    }

    private fun parseSubModule(project: ProjectModel, node: Node?, path: String) {
        node ?: return
        //解析分组
        node.getAt(QName("group"))?.forEach {
            val group: Node = it as? Node ?: return@forEach
            parseSubModule(project, group, "$path/${group.get("@name")?.toString() ?: ""}")
        }

        node.getAt(QName("submodule"))?.forEach {
            val s: Node = it as? Node ?: return@forEach
            addNewSubModule(project, s, path)
        }
    }

    private fun addNewSubModule(project: ProjectModel, node: Node?, path: String) {
        node ?: return
        val name = node.get("@name")?.toString() ?: return
        val subModule = SubModule(name)
        subModule.path = "$path/$name"
        subModule.introduce = node.get("@introduce")?.toString() ?: ""
        subModule.isApplication = node.get("@type") == "application"
        subModule.project = project
        project.submodules.add(subModule)

        val apiName = node.get("@api")?.toString() ?: return
        if (apiName == "this") {//添加附属api模块
            val api = SubModule("${name}_api")
            api.path = "${subModule.path}/src/api"
            api.introduce = "api for $name"
            api.isApplication = false
            api.project = project
            api.attachModel = subModule

            subModule.apiModel = api
            project.submodules.add(api)
        } else {
            subModule.apiModel = SubModule(apiName)
        }
    }

    fun strToPair(s: String): Pair<String?, String?> {
        val i = s.indexOf(",")
        if (i < 0) return Pair(s, null)
        return Pair(s.substring(0, i), s.substring(i + 1))
    }
}
