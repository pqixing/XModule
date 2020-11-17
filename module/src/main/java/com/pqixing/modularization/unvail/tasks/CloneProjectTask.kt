//package com.pqixing.modularization.unvail.tasks
//
//import com.pqixing.help.Tools
//import com.pqixing.modularization.utils.GitUtils
//import com.pqixing.modularization.base.BaseTask
//import com.pqixing.modularization.setting.ImportPlugin.Companion.getArgs
//import com.pqixing.modularization.utils.ResultUtils
//import com.pqixing.tools.FileUtils
//import java.io.File
//
//open class CloneProjectTask : BaseTask() {
//
//    val clones = ArrayList<String>()
//    val fails = ArrayList<String>()
//    val exists = ArrayList<String>()
//
//    override fun runTask() = project.getArgs().manifest.projects.forEach {
//        val dir = File(project.getArgs().env.codeRootDir, it.path)
//        if (GitUtils.isGitDir(dir)) {
//            exists.add(dir.name)
//            return@forEach
//        }
//        if (dir.exists()) {
//            FileUtils.delete(dir)
//        }
//        Tools.println("          start clone-> ${dir.name} ${it.url}")
//        GitUtils.clone(it.url, dir)?.close()
//        (if (GitUtils.isGitDir(dir)) clones else fails).add("\n${dir.name}:${it.url}")
//    }
//
//    override fun end() {
//        val result = "Clone -> $clones,  Fail -> $fails  exists -> $exists"
//        ResultUtils.writeResult(result, fails.size)
//    }
//}