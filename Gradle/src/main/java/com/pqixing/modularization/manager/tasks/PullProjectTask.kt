//package com.pqixing.modularization.manager.tasks
//
//import com.pqixing.modularization.utils.GitUtils
//import com.pqixing.modularization.base.BaseTask
//import com.pqixing.modularization.manager.ProjectManager
//import com.pqixing.modularization.utils.ResultUtils
//
///**
// * 切换分支
// */
//open class PullProjectTask : BaseTask() {
//    override fun runTask() {
//        val fail = ArrayList<String>()
//        val gits = ProjectManager.findAllGitPath().values.filter { it.exists() }.toMutableList()
//
//        gits.forEach { if (it.exists() && !GitUtils.pull(ProjectManager.findGit(it.absolutePath))) fail.add(it.name) }
//        ResultUtils.writeResult("PullProjectTask -> $fail", fail.size)
//    }
//}