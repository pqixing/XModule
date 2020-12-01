package com.pqixing.help

import com.alibaba.fastjson.JSON
import com.pqixing.Config
import com.pqixing.EnvKeys
import com.pqixing.model.Compile
import com.pqixing.model.ManifestModel
import com.pqixing.model.Module
import com.pqixing.model.ProjectModel
import com.pqixing.tools.CheckUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import groovy.lang.GroovyClassLoader
import groovy.util.Node
import groovy.util.NodeList
import groovy.util.XmlParser
import groovy.xml.QName
import java.io.File
import java.net.URL

object XmlHelper {

    /**
     * 解析出pom文件的all exclude依赖
     */
    fun parsePomExclude(pomText: String, matchGroup: String): MavenPom {
        val pom = MavenPom()
        if (pomText.isEmpty()) return pom
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


    fun parseMetadata(txt: String?): MavenMetadata {
        val mate = MavenMetadata()
        if (txt?.isNotEmpty() != true) return mate
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

    private fun getChildNode(paren: Node?, name: String) = getChildNodeList(paren, name).firstOrNull() as? Node
    private fun getChildNodeList(paren: Node?, name: String): NodeList {
        paren ?: return NodeList()
        val at = paren.getAt(QName(name))
        if (at.size > 0) return at
        return paren.get(name) as NodeList
    }

    private fun getChildNodeValue(paren: Node?, name: String) = getNodeValue(getChildNode(paren, name))
    private fun getNodeValue(node: Node?): String {
        node ?: return ""
        val value = node.value().toString();
        return value.substring(1, value.length - 1)
    }

    internal fun parseManifest(txt: File): ManifestModel {

        val node = XmlParser().parseText(txt.readText())
        val manifest = ManifestModel(node.get("@baseUrl")?.toString() ?: "")
        manifest.mavenUrl = node.get("@mavenUrl")?.toString() ?: ""
        manifest.groupId = node.get("@groupId")?.toString() ?: ""
        manifest.mavenUser = node.get("@mavenUser")?.toString() ?: ""
        manifest.mavenPsw = node.get("@mavenPsw")?.toString() ?: ""
        manifest.basicUrl = node.get("@basicUrl")?.toString() ?: ""
        manifest.createSrc = node.get("@createSrc") == "true"
        manifest.useBranch = node.get("@useBranch") == "true"
        manifest.initVersion = node.get("@initVersion")?.toString() ?: ""
        manifest.fallbacks.addAll((node.get("@fallbacks")?.toString()
                ?: "").split(",").filter { it.isNotEmpty() })

        parseProjects(manifest, node, "")
        //重新匹配api模块
        manifest.allModules().filter { it.api != null && it.api!!.path.isEmpty() }.forEach { it.api = manifest.findModule(it.api!!.name) }

        //加载依赖模块
        manifest.allModules().filter { it.node is Node }.forEach { m ->
            (m.node as? Node)?.getAt(QName("compile"))?.forEach { n -> addCompile(n as? Node, manifest, m.compiles) }
            (m.node as? Node)?.getAt(QName("devCompile"))?.forEach { n -> addCompile(n as? Node, manifest, m.devCompiles) }

            if (m.api != null) m.compiles.add(Compile(m.api!!).apply {
                version = m.apiVersion
                scope = Compile.SCOP_API
            })
            m.node = null
        }

        //添加全局依赖
        node.getAt(QName("foreach"))?.mapNotNull { it as? Node }?.forEach { n ->
            val excludes = n.get("@exclude")?.toString()?.split(",")?.toSet() ?: emptySet()
            manifest.allModules().filter { !excludes.contains(it.name) }.forEach { m ->
                n.getAt(QName("compile"))?.forEach { n -> addCompile(n as? Node, manifest, m.compiles) }
                n.getAt(QName("devCompile"))?.forEach { n -> addCompile(n as? Node, manifest, m.devCompiles) }
            }
        }
        //解析file模版
        node.getAt(QName("files"))?.mapNotNull { it as? Node }?.forEach { n ->
            n.getAt(QName("item"))?.mapNotNull { it as? Node }?.forEach { f ->
                manifest.files[f.get("@name")?.toString() ?: ""] = f.get("@file")?.toString() ?: ""
            }
        }
        return manifest
    }

    /**
     * 添加依赖信息
     */
    private fun addCompile(node: Node?, manifest: ManifestModel, container: MutableList<Compile>) {
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
        val module = manifest.findModule(name) ?: return
        val compile = Compile(module)
        compile.branch = branch
        compile.version = version
        compile.scope = node.get("@scope")?.toString() ?: Compile.SCOP_API
        compile.justApi = node.get("@justApi")?.toString() == "true"

        node.get("@excludes")?.toString()?.split(",")?.filter { it.isNotEmpty() }?.forEach { s ->
            s.split(":").takeIf { it.size == 2 }?.let { compile.excludes.add(it[0] to it[1]) }
        }

        val apiModule = compile.module.api
        if (apiModule == null) {
            if (compile.module.type != Module.TYPE_APP) container.add(compile)
            return
        }
        //先尝试加载
        val api = Compile(apiModule).apply {
            this.branch = compile.branch
            this.version = module.apiVersion
            attach = compile
            dpType = compile.dpType
            this.scope = compile.scope
        }
        container.add(api)

        compile.scope = Compile.SCOP_RUNTIME
        if (compile.module.type != Module.TYPE_APP && !compile.justApi) container.add(compile)
    }

    private fun parseProjects(manifest: ManifestModel, node: Node?, path: String) {
        node ?: return
        //解析分组
        node.getAt(QName("group"))?.forEach {
            val group: Node = it as? Node ?: return@forEach
            parseProjects(manifest, group, "$path/${group.get("@name")?.toString() ?: ""}")
        }

        node.getAt(QName("project")).forEach {
            val p: Node = it as? Node ?: return@forEach

            val name = p.get("@name").toString()
            val desc = p.get("@desc")?.toString() ?: ""


            //该工程的git地址
            var gitUrl: String = p.get("@url")?.toString() ?: ""

            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "${manifest.baseUrl}/$name.git"

            val project = ProjectModel(manifest, name, "$path/$name", desc, gitUrl)
            project.groupId = p.get("@groupId")?.toString() ?: manifest.groupId

            manifest.projects.add(project)

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
        module.file = node.get("@file")?.toString() ?: "$${module.type}"
        module.groupId = node.get("@groupId")?.toString() ?: project.manifest.groupId
        module.version = node.get("@version")?.toString() ?: ""
        module.node = node
        project.modules.add(module)


        val apiStr = node.get("@api")?.toString()?.split(":") ?: return
        val apiName = apiStr[0]

        module.apiVersion = apiStr.takeIf { it.size >= 2 }?.get(1) ?: ""
        module.api = Module(apiName, project)
    }

    fun strToPair(s: String): Pair<String?, String?> {
        val i = s.indexOf(",")
        if (i < 0) return Pair(s, null)
        return Pair(s.substring(0, i), s.substring(i + 1))
    }

    fun parseInclude(manifest: ManifestModel, sources: Set<String>): MutableSet<String> {
        val includes = sources.toMutableSet()
        val temp = mutableSetOf<String>()
        includes.filter { it.startsWith("D#") }.also { includes.removeAll(it) }.map { it.substring(2) }.forEach { loadAll(manifest, temp, it) }
        includes.addAll(temp)

        includes.filter { it.startsWith("E#") }.also { includes.removeAll(it) }.map { it.substring(2) }.forEach { includes.remove(it) }

        temp.clear()
        includes.filter { it.startsWith("ED#") }.also { includes.removeAll(it) }.map { it.substring(3) }.forEach { loadAll(manifest, temp, it) }
        includes.removeAll(temp)

        return includes
    }

    private fun loadAll(manifest: ManifestModel, includes: MutableSet<String>, target: String) {
        includes.add(target)

        val requests = manifest.findModule(target)?.compiles?.map { it.name } ?: return

        val reLoads = requests.filter { !includes.contains(it) }

        //如果已经加载过，不重复加载
        includes.addAll(requests)

        for (request in reLoads) loadAll(manifest, includes, request)
    }


    fun loadConfig(basePath: String, extras: Map<String, Any?> = emptyMap()): Config {

        val config = try {
            val configFile = fileConfig(basePath)
            if (!configFile.exists()) FileUtils.writeText(configFile, FileUtils.fromRes(configFile.name))
            val parseClass = GroovyClassLoader().parseClass(configFile)
            JSON.parseObject(JSON.toJSONString(parseClass.newInstance()), Config::class.java)
        } catch (e: Exception) {
            Config()
        }
        /**
         * 从系统配置中加载对应的变量
         */
        config.javaClass.fields.forEach {
            val key = it.name
            it.isAccessible = true

            //从ext或者gradle.properties中读取配置信息
            kotlin.runCatching {
                val extValue = extras[key]?.toString()
                if (extValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, extValue.toBoolean())
                    String::class.java -> it.set(config, extValue)
                }
            }

            //从传入的环境中读取配置信息
            kotlin.runCatching {
                val envValue = TextUtils.getSystemEnv(key)
                if (envValue != null) when (it.type) {
                    Boolean::class.java -> it.setBoolean(config, envValue.toBoolean())
                    String::class.java -> it.set(config, envValue)
                }
            }
        }
        return config
    }

    fun loadManifest(basePath: String?): ManifestModel? = fileManifest(basePath).takeIf { it.exists() }?.let { parseManifest(it) }
    fun loadAllModule(basePath: String?) = loadManifest(basePath)?.allModules() ?: mutableSetOf()
    fun saveConfig(basePath: String, config: Config): Boolean {
        val configFile = File(basePath, EnvKeys.USER_CONFIG)
        if (!configFile.exists()) FileUtils.writeText(configFile, FileUtils.fromRes(EnvKeys.USER_CONFIG))


        var result = FileUtils.readText(configFile) ?: return false
        /**
         * 从系统配置中加载对应的变量
         */
        Config::class.java.fields.forEach { f ->
            f.isAccessible = true
            val value = f.get(config)?.let { if (it is String) "\"$it\"" else "$it" }
            result = result.replace(Regex("String *${f.name} *=.*;"), "String ${f.name} = $value;")
        }
        return FileUtils.writeText(configFile, result, true).isNotEmpty()
    }

    fun loadVersionFromNet(basePath: String?) {
        val manifest = loadManifest(basePath) ?: return
        val versionDir = fileVersion(basePath, manifest.mavenUrl)

        val fromUrl = { name: String -> readUrlTxt(manifest.fullUrl(name, EnvKeys.XML_META)).also { FileUtils.writeText(File(versionDir, "${name}.xml"), it) } }

        //更新基础版本记录清单文件
        val lastLog = parseMetadata(fromUrl(EnvKeys.BASIC)).versions.lastOrNull() ?: "default"
        //更新增加更新的版本记录文件
        fromUrl("${EnvKeys.BASIC_LOG}/$lastLog")
        //更新分支tag的记录文件
        fromUrl(EnvKeys.BASIC_TAG)
    }


    fun fileVersion(basePath: String?, url: String = loadManifest(basePath)?.mavenUrl
            ?: "default"): File = File(basePath, "build/${EnvKeys.XMODULE}/version/${url.hashCode()}")

    fun fileBasic(basePath: String?): File = File(basePath, EnvKeys.BASIC)
    fun fileConfig(basePath: String?): File = File(basePath, EnvKeys.USER_CONFIG)
    fun fileManifest(basePath: String?): File = File(basePath, EnvKeys.XML_MANIFEST)

    /**
     *
     */
    fun readUrlTxt(url: String) = kotlin.runCatching {
        if (url.startsWith("http")) URL(url).readText() else FileUtils.readText(File(url))
    }.getOrNull() ?: ""

}
