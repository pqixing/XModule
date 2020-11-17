//package com.pqixing.modularization.unvail.tasks
//
//import com.pqixing.EnvKeys
//import com.pqixing.help.getEnvValue
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.base.BaseTask
//import com.pqixing.modularization.setting.ImportPlugin.Companion.getArgs
//import com.pqixing.modularization.utils.GitUtils
//import com.pqixing.modularization.utils.ResultUtils
//import java.io.File
//
///**
// * 切换分支
// */
//open class CheckOutTask : BaseTask() {
//    init {
//        group = Keys.GROUP_OTHER
//    }
//
//    override fun runTask() {
//        var targetBranch = EnvKeys.opBranch.getEnvValue() ?: return
//
//        val fail = ArrayList<String>()
//        GitUtils.open(project.getArgs().env.basicDir)?.apply {
//            val check = GitUtils.checkoutBranch(this, targetBranch, true)
//            if (!check) fail.add(project.rootDir.name)
//            close()
//        }
//        project.getArgs().manifest.projects.forEach {
//            val dir = File(project.getArgs().env.codeRootDir, it.path)
//            if (!GitUtils.isGitDir(dir)) return@forEach
//            GitUtils.open(dir)?.apply {
//                val check = GitUtils.checkoutBranch(this, targetBranch, true)
//                if (!check) fail.add(dir.name)
//                close()
//            }
//        }
//
//        ResultUtils.writeResult("CheckOutTask -> $fail", fail.size)
//    }
//}