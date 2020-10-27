package com.pqixing.modularization.maven

import com.pqixing.EnvKeys
import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.helper.IExtHelper
import com.pqixing.modularization.helper.JGroovyHelper
import com.pqixing.modularization.root.getArgs
import com.pqixing.modularization.setting.ArgsExtends
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.TextUtils
import org.apache.commons.codec.digest.DigestUtils

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


    private fun allBranches(): Set<String> {
        //当前所有的版本号的key
        args.config.sync = true
        return args.vm.readCurVersions().keys.map { it.replace(args.manifest.groupId + ".", "").substringBefore(".") }.toSet()
    }

    /**
     * 从网络获取最新的版本号信息
     */
    private fun parseVersion(excludes: List<String> = emptyList()) {
        val versions = HashMap<String, Int>()
        Tools.println("parseVersion  start ->")
        val start = System.currentTimeMillis()
        //上传版本号到服务端
        reloadVersion(args.manifest.fullMavenUrl(), allBranches().minus(excludes), versions)

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

    fun reloadVersion(mavenUrl: String, branchs: Set<String>, versions: HashMap<String, Int>) {
        val modules = args.manifest.allModules().map { it.name }

        for (b in branchs) for (m in modules) {

            val url = TextUtils.append(arrayOf(mavenUrl, b.replace(".", "/"), m, "maven-metadata.xml"))
            val metaStr = XmlHelper.readUrlTxt(url)
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