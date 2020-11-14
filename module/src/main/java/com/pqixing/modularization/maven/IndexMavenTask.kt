package com.pqixing.modularization.maven

import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.base.getArgs
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

/**
 * 同步工程的代码和分支,为了Jekens 构建使用
 */
open class IndexMavenTask : BaseTask() {
    lateinit var args: ArgsExtends
    override fun prepare() {
        super.prepare()
        this.dependsOn("uploadArchives")
    }

    override fun whenReady() {
        super.whenReady()
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        args = project.getArgs()
        val branches = args.config.opts?.toString()?.split(",")?.filter { it.isNotEmpty() }
        if (branches?.isNotEmpty() != true) {//全量打包
            extHelper.setMavenInfo(project, args.manifest.groupId, EnvKeys.BASIC, System.currentTimeMillis().toString(), "")
            parseVersion()
        } else {
            val hash = DigestUtils.md5Hex(branches.first());
            extHelper.setMavenInfo(project, args.manifest.groupId, EnvKeys.BASIC_TAG, hash + "-" + System.currentTimeMillis().toString(), "")
            parseVersion(branches)
        }
    }

    fun readAllBranches(): Set<String> {
        val groupId = args.manifest.groupId
        val branchs = mutableSetOf<String>()
        val basePath = args.env.rootDir.absolutePath
        val versionDir = XmlHelper.fileVersion(basePath)
        //加载所有版本信息相关的文件
        val versions = XmlHelper.parseMetadata(FileUtils.readText(File(versionDir, "${EnvKeys.BASIC}.xml"))).versions
        val toIndex = versions.size - 1

        val format = { txt: String ->
            for (l in txt.lines()) if (l.contains(groupId)) {
                val line = l.replace("=", ".")
                val startIndex = line.indexOf(groupId) + groupId.length + 1
                val array = line.substring(startIndex).split(".")
                if (array.size > 4) {
                    branchs += array.subList(0, array.size - 4).joinToString(".")
                }
            }
        }

        for (version in versions.subList((toIndex - 10).coerceAtLeast(0), toIndex)) {
            if (version != "default") {
                format(XmlHelper.readUrlTxt(args.manifest.fullUrl(EnvKeys.BASIC, version, "${EnvKeys.BASIC}-${version}.txt")))
            }
            //根据full版本，加载临时提交记录进行合并
            format(XmlHelper.readUrlTxt(args.manifest.fullUrl(EnvKeys.BASIC_LOG, version, EnvKeys.XML_META)))
        }

        branchs += args.manifest.fallbacks
        return branchs.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    /**
     * 从网络获取最新的版本号信息
     */
    private fun parseVersion(excludes: List<String> = emptyList()) {
        val versions = HashMap<String, Int>()
        Tools.println("parseVersion  start ->")
        val start = System.currentTimeMillis()
        //上传版本号到服务端
        reloadVersion(args.manifest.fullUrl(), readAllBranches().minus(excludes), versions)

        Tools.println("parseVersion  end -> ${System.currentTimeMillis() - start} ms -> $excludes")
        args.vm.storeToUp(versions)
    }

    override fun runTask() {
        //等待两秒
        Thread.sleep(300)
        //更新本地版本信息
        XmlHelper.loadVersionFromNet(args.env.rootDir.absolutePath)
        ResultUtils.writeResult(args.env.defArchivesFile.absolutePath)
    }

    private fun reloadVersion(mavenUrl: String, branchs: Set<String>, versions: HashMap<String, Int>) {
        Tools.println("reloadVersion -> $mavenUrl -> ${branchs}")
        val modules = args.manifest.allModules().map { it.name }

        for (b in branchs) for (m in modules) {

            val url = args.manifest.fullUrl(b.replace(".", "/"), m, EnvKeys.XML_META)
            val metaStr = XmlHelper.readUrlTxt(url)
//            Tools.println("request -> $url -> ${metaStr}")
            if (metaStr.isNotEmpty()) kotlin.runCatching {
                val meta = XmlHelper.parseMetadata(metaStr)
                Tools.println("request -> $url -> ${meta.versions}")
                addVersion(versions, meta.groupId.trim(), meta.artifactId.trim(), meta.versions)
            }
        }
    }

    /**
     * 把每个版本的最后版本号添加
     */
    private fun addVersion(curVersions: HashMap<String, Int>, groupId: String, artifactId: String, version: List<String>) {
//        Tools.println("addVersion -> $groupId $artifactId $version")
        val addV = StringBuffer()
        //倒叙查询
        for (i in version.size - 1 downTo 0) {
            val v = version[i]
            val l = v.lastIndexOf('.')
            val bv = v.substring(0, l)
            val lv = v.substring(l + 1)
            val key = "$groupId.$artifactId.$bv"
            val latKey = curVersions[key] ?: 0
            val newKey = lv.toIntOrNull() ?: 0
            if (newKey >= latKey) {
                curVersions[key] = newKey
                addV.append(v).append(",")
            }
        }
    }

}