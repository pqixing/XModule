package com.pqixing.help

import com.pqixing.Tools
import com.pqixing.model.Compile
import com.pqixing.model.ProjectModel
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.Module
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
    fun parsePomExclude(pomText: String, matchGroup: String): MavenPom {
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
                pom.dependency.add("$gid:${getChildNodeValue(dependency, "artifactId")}:${getChildNodeValue(dependency, "version")}")
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
        xmlModel.basicUrl = node.get("@basicUrl")?.toString() ?: ""
        xmlModel.mavenUrl = node.get("@mavenUrl")?.toString() ?: ""
        xmlModel.group = node.get("@group")?.toString() ?: ""
        xmlModel.mavenUser = node.get("@mavenUser")?.toString() ?: ""
        xmlModel.mavenPsw = node.get("@mavenPsw")?.toString() ?: ""
        xmlModel.createSrc = node.get("@createSrc") == "true"
        xmlModel.baseVersion = node.get("@baseVersion")?.toString() ?: ""
        xmlModel.matchingFallbacks.addAll((node.get("@matchingFallbacks")?.toString()
                ?: "").split(",").filter { it.isNotEmpty() })

        parseProjects(xmlModel, node, "")
        //重新匹配api模块
        xmlModel.allModules().filter { it.api != null && it.api!!.path.isEmpty() }.forEach { it.api = xmlModel.findModule(it.api!!.name) }
        //加载依赖模块
        xmlModel.allModules().filter { it.node is Node }.forEach { m ->
            (m.node as? Node)?.getAt(QName("compile"))?.forEach { n -> addCompile(n as? Node, xmlModel, m.compiles) }
            (m.node as? Node)?.getAt(QName("devCompile"))?.forEach { n -> addCompile(n as? Node, xmlModel, m.devCompiles) }
            m.api?.let {
                m.compiles.add(Compile(it).apply {
                    version = m.apiVersion
                    matchAuto = true
                    scope =Compile.SCOP_API
                })
            }
            m.node = null
        }
        //添加全局依赖
        node.getAt(QName("foreach"))?.mapNotNull { it as? Node }?.forEach { n ->
            val excludes = n.get("@exclude")?.toString()?.split(",")?.toSet() ?: emptySet()
            xmlModel.allModules().filter { !excludes.contains(it.name) }.forEach { m ->
                n.getAt(QName("compile"))?.forEach { n -> addCompile(n as? Node, xmlModel, m.compiles) }
                n.getAt(QName("devCompile"))?.forEach { n -> addCompile(n as? Node, xmlModel, m.devCompiles) }
            }
        }

        xmlModel.allModules().forEach { if (it.version.isEmpty()) it.version = xmlModel.baseVersion }
        //
        node.localText()
        return xmlModel
    }

    /**
     * 添加依赖信息
     */
    private fun addCompile(node: Node?, projectXml: ProjectXmlModel, container: MutableList<Compile>) {
        node ?: return

        val nameStr = node.get("@name")?.toString() ?: return
        //根据 ： 号分割
        val split = nameStr.split(":")
        var name = ""
        var branch = ""
        var version = ""

        when (split.size) {
            1 -> {
                branch = ""
                name = split[0]
                version = ""
            }
            2 -> {
                branch = ""
                name = split[0]
                version = split[1]
            }
            3 -> {
                branch = split[0]
                name = split[1]
                version = split[2]
            }
            else -> Tools.printError(-1, "DpsExtends compile illegal name -> $name")
        }
        val module = projectXml.findModule(name) ?: return
        val compile = Compile(module)
        compile.branch = branch
        compile.version = version
        compile.scope = node.get("@scope")?.toString() ?: Compile.SCOP_API
        compile.justApi = node.get("@justApi")?.toString() == "true"

        node.get("@excludes")?.toString()?.split(",")?.filter { it.isNotEmpty() }?.forEach { s ->
            s.split(":").takeIf { it.size == 2 }?.let { compile.excludes.add(it[0] to it[1]) }
        }

        compile.version = compile.version.replace("*", "")
        compile.matchAuto = compile.version.contains("*")

        val apiModule = compile.module.findApi()
        if (apiModule == null) {
            if (!compile.module.isApplication) container.add(compile)
            return
        }
        //先尝试加载
        val api = Compile(apiModule).apply {
            branch = compile.branch
            version = ""
            matchAuto = true
            attach = compile
            dpType = compile.dpType
            this.scope = compile.scope
        }
        container.add(api)

        compile.scope = Compile.SCOP_RUNTIME
        if (!compile.module.isApplication && !compile.justApi) container.add(compile)
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
            var desc = p.get("@desc")?.toString() ?: ""


            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString() ?: ""

            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "${xmlModel.baseUrl}/$name.git"

            val project = ProjectModel(name, "$path/$name", desc, gitUrl)


            xmlModel.projects.add(project)

            //type不为空，本身也作为一个模块添加
            if (p.get("@type") != null) addNewModule(project, p, path)

            parseModule(project, p, path + "/" + p.get("@name")?.toString())
        }
    }

    private fun parseModule(project: ProjectModel, node: Node?, path: String) {
        node ?: return
        //解析分组
        node.getAt(QName("group"))?.forEach {
            val group: Node = it as? Node ?: return@forEach
            parseModule(project, group, "$path/${group.get("@name")?.toString() ?: ""}")
        }

        node.getAt(QName("module"))?.forEach {
            val s: Node = it as? Node ?: return@forEach
            addNewModule(project, s, path)
        }
    }

    private fun addNewModule(project: ProjectModel, node: Node?, path: String) {
        node ?: return
        val realName = node.get("@name")?.toString() ?: return

        val name = node.get("@alias")?.toString()?.takeIf { it.trim().isNotEmpty() } ?: realName

        val module = Module(name, project)

        module.path = "$path/$realName"
        module.desc = node.get("@desc")?.toString() ?: ""
        module.type = node.get("@type")?.toString() ?: "library"
        module.merge = node.get("@merge")?.toString() ?: ""
        module.version = node.get("@version")?.toString() ?: ""
        module.transform = (node.get("@transform")?.toString() ?: "true") == "true"
        module.node = node
        project.modules.add(module)


        val apiStr = node.get("@api")?.toString()?.split(":") ?: return
        val apiName = apiStr[0]

        module.apiVersion = apiStr.takeIf { it.size >= 2 }?.get(1) ?: ""
        if (apiName == "this") {//添加附属api模块
            val api = Module("${name}_api", project)
            api.path = "${module.path}/src/api"
            api.desc = "api for $name"
            api.type = "library"
            api.attach = module
            module.api = api
            project.modules.add(api)
        } else {
            module.api = Module(apiName, project)
        }
    }

    fun strToPair(s: String): Pair<String?, String?> {
        val i = s.indexOf(",")
        if (i < 0) return Pair(s, null)
        return Pair(s.substring(0, i), s.substring(i + 1))
    }
}