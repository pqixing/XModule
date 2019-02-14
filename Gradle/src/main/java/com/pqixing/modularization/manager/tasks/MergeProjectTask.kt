package com.pqixing.modularization.manager.tasks

import com.pqixing.Tools
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.manager.ManagerPlugin
import com.pqixing.modularization.manager.ProjectManager
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.modularization.utils.init
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.MergeResult
import java.io.File

/**
 * 检查需要合并的工程，列出名称和路径
 * 1,下载全部工程代码
 * 2,更新全部代码
 * 3,更新版本信息，查询该分支的仓库中所有上传的模块，列出，合并后需要重新编译这些模块
 * 4,检查所有工程代码是否是Clean,是否和Root分支一致
 * 5，合并所有工程，列出所有冲突工程目录
 * 6，结合1，给出所有需要导入的模块列表（包含要重新编译和有冲突的工程模块），标记需要处理冲突的分支
 */
open class MergeProjectTask : BaseTask() {

    init {
        //先更新版本信息，以免出现问题
        this.dependsOn("CloneProject", "PullProject")
        project.getTasksByName("PullProject", false)?.forEach { it.mustRunAfter("CloneProject") }
    }


    override fun runTask() {

        val rootBranch = ProjectManager.rootBranch

        val unClean = ArrayList<String>()
        val wrongBranch = ArrayList<String>()
        val allGitPath = ProjectManager.findAllGitPath().toMutableMap()
        allGitPath["CodeManager"] = ProjectManager.projectRoot
        //检查当前分支是否跟rootBranch一致，是否有未Clean的文件
        allGitPath.forEach {
            val git = ProjectManager.findGit(it.value.absolutePath)
            if (!GitUtils.checkIfClean(git)) unClean.add(it.value.name)
            val b = git?.repository?.branch
            if (rootBranch != b) wrongBranch.add("${it.value.name}:$b")
        }

        val error = StringBuilder()
        if (unClean.isNotEmpty()) {
            error.append("unClean -> $unClean")
        }
        if (wrongBranch.isNotEmpty()) {
            error.append("wrongBranch -> $unClean")
        }
        if (error.isNotEmpty()) Tools.printError("Exception -> please check log, $error")

        val info = ManagerPlugin.getManagerPlugin().projectInfo
        var targetBranch = info.taskBranch
        if (targetBranch.isEmpty()) Tools.printError("Exception -> branch to merge is empty!!!")


        val configGit = HashSet<String>()
        allGitPath.forEach {
            val git = ProjectManager.findGit(it.value.absolutePath) ?: return@forEach
            val ref = GitUtils.findBranchRef(git, targetBranch, true) ?: return@forEach
            val call = git.merge().setCommit(true).include(ref).init().call()
            Tools.println("Merge ${it.value.name} $rootBranch -> $targetBranch ${call.mergeStatus}")
            if (call.mergeStatus == MergeResult.MergeStatus.CONFLICTING) configGit.add(it.value.name)
        }
        val result = StringBuilder("Merge result")
        result.append("\n Config Git Project -> $configGit")


        //查出当前分支所有提交过的模块（合并完成后需要编译的版本）
        VersionManager.indexVersionFromNet()
        val byBranch = VersionManager.findAllModuleByBranch(rootBranch)
        Tools.println("All module by branch $rootBranch-> $byBranch")

        val mergeFile = File(ManagerPlugin.getManagerPlugin().cacheDir, Keys.MERGE_RESULT)

        result.append("\n Request build to maven module -> $byBranch")

        //用来处理
        val resolverConflict = HashSet<String>()
        byBranch.forEach {
            val gitName = ProjectManager.findComponent(it)?.rootName ?: return@forEach
            if (configGit.remove(gitName)) resolverConflict.add("$it:$gitName")
        }

        val suggestImport = mutableSetOf<String>()
        suggestImport.addAll(byBranch)

        if (configGit.isNotEmpty()) ProjectManager.findAllComponent().forEach {
            if (configGit.remove(it.rootName)) {
                resolverConflict.add("${it.name}:${it.rootName}")
                suggestImport.add(it.name)
            }
        }
        result.append("\n Try to resolver conflict  -> $byBranch ")

        result.append("\n Suggest import module -> $suggestImport")

        //输出日志
        FileUtils.writeText(mergeFile, result.toString())

        ResultUtils.writeResult(mergeFile.absolutePath)
    }
}