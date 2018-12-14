package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.git.Components
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.AndroidPlugin
import com.pqixing.modularization.android.dps.DpComponents
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.iterface.IExtHelper
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.shell.Shell
import com.pqixing.tools.UrlUtils
import java.util.*

/**
 * 上传到Maven之前检查
 */
open class ToMavenCheckTask : BaseTask() {
    init {
        group = ""
    }

    override fun start() {

    }

    override fun runTask() {
        val extends = ManagerPlugin.getManagerExtends()!!
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)

        val plugin = AndroidPlugin.getPluginByProject(project)
        val dpsExtends = plugin.getExtends(DpsExtends::class.java)
        arrayOf(dpsExtends.apiCompiles, dpsExtends.compiles)

        val component = ProjectManager.findComponent(project.name)
        val lastLog = component.lastLog
        val artifactId = if (plugin.APP_TYPE == Components.TYPE_LIBRARY_API && plugin.BUILD_TYPE == Components.TYPE_LIBRARY_API) {
            checkLocalDps(dpsExtends.apiCompiles)
            "${project.name}_api"
        } else {
            checkLocalDps(dpsExtends.compiles)
            project.name
        }

        checkLoseDps(plugin.dpsManager.loseList)

        val groupId = "${extends.groupName}.${lastLog.branch}"
        val baseVersion = dpsExtends.toMavenVersion
        checkBaseVersion(baseVersion)

        val v = VersionManager.getNewerVersion(lastLog.branch, artifactId, baseVersion)
        checkLastLog(component, artifactId, baseVersion, v)

        val version = "$baseVersion.${v + 1}"

        val name = "${Keys.PREFIX_LOG}?hash=${lastLog.hash}&commitTime=${lastLog.commitTime}&message=${lastLog.message}&desc=${dpsExtends.toMavenDesc}"
        extHelper.setMavenInfo(project
                , extends.groupMaven
                , extends.mavenUserName
                , extends.mavenPassWord
                , groupId
                , artifactId
                , version
                , name)

        extHelper.setExtValue(project, Keys.LOG_VERSION, version)
        extHelper.setExtValue(project, Keys.LOG_BRANCH, lastLog.branch)
        extHelper.setExtValue(project, Keys.LOG_MODULE, artifactId)
    }

    /**
     * 检查上一个提交版本的日志，如果日志一样，则不允许上传
     */
    private fun checkLastLog(component: Components, artifactId: String, baseVersion: String, v: Int) {
        val curLog = component.lastLog
//        Shell.runSync("git remote update", curLog.gitDir)
//        //获取远程分支最后的版本号
//        val lastRemoteHash = Shell.runSync("git log -1 origin/${curLog.branch} ${if (component.name == component.rootName) "" else component.name}", curLog.gitDir)[0].trim()
//        if (curLog.hash != lastRemoteHash) {
//            Tools.printError("${component.name} Local code is different from remote,Please update your code or Check whether it needs to be push")
//        }
        //检查Maven仓库最后的一个版本的信息
        var lastVersion = v
        var matchBranch = curLog.branch
        val match = ManagerPlugin.getManagerExtends().matchingFallbacks
        var i = match.indexOf(matchBranch)
        while (v < 0 && i < match.size) {
            matchBranch = if (i < 0) curLog.branch else match[i]
            lastVersion = VersionManager.getNewerVersion(matchBranch, artifactId, baseVersion)
            i++
        }
        //一条记录都没有，新组件
        if (lastVersion < 0) return

        //如果匹配到的版本不是当前分支，则提示升级版本号
        if (matchBranch != curLog.branch) {
            Tools.printError("${component.name} Not allow user the same base version on new branch , please update before ToMaven!!!")
        }
        val params = UrlUtils.getParams(DpsManager.getPom(matchBranch, artifactId, "$baseVersion.$lastVersion").name)
        val hash = params["hash"] ?: ""
        val commitTime = params["commitTime"]?.toInt() ?: 0
        if (hash == curLog.hash || curLog.commitTime < commitTime) {
            Tools.printError("${component.name} There are not change after last ToMaven!!!")
        }
    }


    private fun checkBaseVersion(baseVersion: String) {
        if (!VersionManager.isBaseVersion(baseVersion)) Tools.printError("ToMavenCheckTask $baseVersion is not base version, try x.x etc: 1.0")
    }

    private fun checkLoseDps(loseList: MutableList<String>) {
        if (loseList.isNotEmpty()) {
            Tools.printError("${project.name}  There are some dependency lose!! -> $loseList")
        }
    }

    private fun checkLocalDps(compiles: HashSet<DpComponents>) {
        val map = compiles.filter { it.localCompile }.map { it.moduleName }
        if (map.isNotEmpty()) {
            Tools.printError("${project.name} Contain local project, please remove it before upload -> $map")
        }
    }


    override fun end() {
    }
}