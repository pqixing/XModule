package com.pqixing.modularization.maven

import com.pqixing.Tools
import com.pqixing.model.SubModule
import com.pqixing.modularization.IExtHelper
import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.MDPlugin
import com.pqixing.modularization.android.dps.DpComponents
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.getArgs
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.TextUtils
import com.pqixing.tools.UrlUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.util.*

/**
 * 上传到Maven之前检查
 */
open class ToMavenCheckTask : BaseTask() {
    init {
        group = ""
        val mdPlugin = project.MDPlugin()
        if (mdPlugin.subModule.hasAttach()) {//如果是Api模块，则依赖主模块的clean方法，强行导入主模块
            this.dependsOn(":${mdPlugin.subModule.attachModel?.name}:clean")
        }
    }

    /**
     * toMaven时,忽略检查项目, oldType 兼容旧版ide插件,newType 类型可以组合使用
     * oldType 0, newType:0   :UnCheck null
     * oldType 1, newType:1<<4:UnCheck branch 不校验新分支第一次提交是否需要升级版本号
     * oldType 2, newType:1<<5:UnCheck version  不校验是否和上次代码是否相同,允许提交重复
     * oldType 3, newType:1<<6:UnCheck change  不检验本地是否存在未提交修改
     */
    var unCheck = 0

    override fun start() {
        try {
            unCheck = project.getArgs().config.toMavenUnCheck.toInt()
        } catch (e: Exception) {
            Tools.println(e.toString())
        }
    }

    override fun runTask() {
        val extends = project.getArgs()
        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)

        val plugin = project.MDPlugin()
        val dpsExtends = plugin.getExtends(DpsExtends::class.java)
        val subModule = plugin.subModule
//        val lastLog = plugin.subModule
        val artifactId = subModule.name
        if (subModule.getBranch() != extends.env.templetBranch) {
            Tools.println(unCheck(1), "${subModule.name} branch is ${subModule.getBranch()} , Doc branch ${extends.env.templetBranch} does not match")
            return
        }

        val open = GitUtils.open(File(extends.env.codeRootDir, subModule.project.name))
        if (open == null) {
            Tools.printError(-1, "${subModule.project.name} Git open fail, please check")
            return
        }

        checkLocalDps(dpsExtends.compiles)

        checkLoseDps(plugin.dpsManager.loseList)

        val branch = open.repository.branch
        val groupId = "${extends.projectXml.mavenGroup}.$branch"
        val baseVersion = dpsExtends.toMavenVersion
        checkBaseVersion(baseVersion)

        checkGitStatus(open, subModule)

        val v = project.getArgs().versions.getNewerVersion(branch, artifactId, baseVersion)
        val revCommit = loadGitInfo(open, subModule)
        if (revCommit == null) {
            Tools.printError(-1, "${subModule.name} Can not load git info!!")
            return
        }
        checkLastLog(revCommit, artifactId, branch, baseVersion, v)

        val version = "$baseVersion.${v + 1}"

        val name = "${Keys.PREFIX_LOG}?hash=${revCommit.name}&commitTime=${revCommit.commitTime}&message=${revCommit.fullMessage}&desc=${dpsExtends.toMavenDesc}"
        extHelper.setMavenInfo(project
                , extends.projectXml.mavenUrl
                , extends.projectXml.mavenUser.takeIf { it.isNotEmpty() } ?: extends.config.userName
                , extends.getPsw(extends.projectXml.mavenPsw.takeIf { it.isNotEmpty() } ?: extends.config.passWord)
                , groupId
                , artifactId
                , version
                , name)

        extHelper.setExtValue(project, Keys.LOG_VERSION, version)
        extHelper.setExtValue(project, Keys.LOG_BRANCH, branch)
        extHelper.setExtValue(project, Keys.LOG_MODULE, artifactId)
    }

    private fun checkGitStatus(git: Git, subModule: SubModule) {
        if (!GitUtils.checkIfClean(git, getRelativePath(subModule.path))) {
            Tools.println(unCheck(3), "${subModule.name} Code not clean")
        }
    }

    /**
     * 检查上一个提交版本的日志，如果日志一样，则不允许上传
     */
    private fun checkLastLog(revCommit: RevCommit?, artifactId: String, branch: String, baseVersion: String, v: Int) {
        revCommit ?: return
//        Shell.runSync("git remote update", curLog.gitDir)
//        //获取远程分支最后的版本号
//        val lastRemoteHash = Shell.runSync("git log -1 origin/${curLog.branch} ${if (component.name == component.rootName) "" else component.name}", curLog.gitDir)[0].trim()
//        if (curLog.hash != lastRemoteHash) {
//            Tools.printError("${component.name} Local code is different from remote,Please update your code or Check whether it needs to be push")
//        }

        //检查Maven仓库最后的一个版本的信息
        var lastVersion = v
        var matchBranch = branch
        val match = project.getArgs().projectXml.matchingFallbacks
        var i = match.indexOf(matchBranch)
        while (lastVersion < 0 && i < match.size) {
            matchBranch = if (i < 0) branch else match[i]
            lastVersion = project.getArgs().versions.getNewerVersion(matchBranch, artifactId, baseVersion)
            i++
        }
        //一条记录都没有，新组件
        if (lastVersion < 0) return

        //如果匹配到的版本不是当前分支，则提示升级版本号
        if (matchBranch != branch) {
            Tools.println(unCheck(1), "$artifactId Not allow user the same base version on new branch")
        }
        val params = UrlUtils.getParams(DpsManager.getPom(project,matchBranch, artifactId, "$baseVersion.$lastVersion").name)
        val hash = params["hash"] ?: ""
        val commitTime = params["commitTime"]?.toInt() ?: 0
        if (hash == revCommit.name || revCommit.commitTime < commitTime) {
            //距离上次提交没有变更时,视为成功
            ResultUtils.writeResult("$matchBranch:$artifactId:$baseVersion.$lastVersion The code are not change", 0, unCheck(2) != 0)
        }
    }

    /**
     * 检查是否需要忽略错误
     * @return 返回结果 0,uncheckType, <0 , request check
     */
    private fun unCheck(oldType: Int): Int {
        if (unCheck == 0) return -1
        if (unCheck < 4) return unCheck - oldType

        val newType = 1 shl (oldType + 3)
        return Math.min(0, (unCheck and newType) - 1)
    }

    private fun checkBaseVersion(baseVersion: String) {
        if (!TextUtils.isBaseVersion(baseVersion)) Tools.printError(-1, "ToMavenCheckTask $baseVersion is not base version, try x.x etc: 1.0")
    }

    private fun checkLoseDps(loseList: MutableList<String>) {
        if (loseList.isNotEmpty()) {
            Tools.printError(-1, "${project.name}  There are some dependency lose!! -> $loseList")
        }
    }

    private fun checkLocalDps(compiles: HashSet<DpComponents>) {
        val map = compiles.filter { it.localCompile }.map { it.moduleName }
        if (map.isNotEmpty()) {
            Tools.printError(-1, "${project.name} Contain local project, please remove it before upload -> $map")
        }
    }


    override fun end() {
    }

    fun getRelativePath(path: String): String? {
        val of = path.indexOf("/")
        return if (of > 0) return path.substring(of + 1) else null
    }

    fun loadGitInfo(git: Git, subModule: SubModule): RevCommit? {
        val command = git.log().setMaxCount(1)
        getRelativePath(subModule.path)?.apply { command.addPath(this) }
        return command.call().find { true }
    }


}