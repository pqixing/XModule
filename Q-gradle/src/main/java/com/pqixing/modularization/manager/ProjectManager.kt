package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.model.SubModule
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

object ProjectManager {


    fun findSubModuleByName(project: Project,name: String) = project.getArgs().projectXml.findSubModuleByName(name)

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project): SubModule? {
        val args =project.getArgs()
        val info = args.config
        val buildDir = info.buildDir.toString().trim()
        //不在配置文件的git工程，不进行管理
        val subModule = args.projectXml.findSubModuleByName(project.name) ?: return null
        val apiModule = subModule.hasAttach()
        //重新设置build 目录
        project.buildDir = File(project.projectDir, "build/" + (if (buildDir.isEmpty()) "default" else buildDir))

        if (subModule.hasCheck) return subModule

        val projectDir = File(args.env.codeRootDir, subModule.project.name)

        val docRepoBranch = args.env.templetBranch
        var mBranch = subModule.project.branch
        if (mBranch.isEmpty()) if (GitUtils.isGitDir(projectDir)) Git.open(projectDir)
        else {
            FileUtils.delete(projectDir)
            GitUtils.clone(subModule.project.url, projectDir,docRepoBranch)
        }?.apply {
            mBranch = this.repository.branch
            subModule.project.branch = mBranch
            GitUtils.close(this)
        }
        if (mBranch != docRepoBranch) {
            Tools.println("Warming!!!  ${subModule.name} branch is $mBranch , do not match doc branch $docRepoBranch")
        }
        //如果是Api工程,检查基础模块在不在
        if (apiModule) {
            val moduleDir = File(args.env.codeRootDir, subModule.path)
            with(File(moduleDir, "build.gradle")) {
                if (!exists()) FileUtils.writeText(this, "apply plugin: 'com.module.android' ")
            }
            val name = TextUtils.numOrLetter(project.name.replace("_api",""))
            val packageName = "${args.projectXml.mavenGroup.replace(".", "/")}/auto/router/${name}api"
            val className = "${TextUtils.firstUp(name)}ApiPaths"
            with(File(moduleDir, "java/$packageName/$className.java")) {
                if (!exists()) FileUtils.writeText(this, "package ${packageName.replace("/", ".")};\nfinal class $className {}")
            }
//            with(File(moduleDir, "resources/values/strings.xml")) {
//                if (!exists()) FileUtils.writeText(this, "<resources></resources>")
//
//            }
            //写入空清单文件
            with(File(moduleDir, "AndroidManifest.xml")) {
                if (!exists()) {
                    val emptyManifest = (FileUtils.readText(File(args.env.templetRoot, "android/Empty_AndroidManifest.xml"))
                            ?: "").replace("[groupName]", args.projectXml.mavenGroup).replace("[projectName]", subModule.name)
                    FileUtils.writeText(this, emptyManifest)
                }
            }
        }
        subModule.hasCheck = true
        return subModule
    }
}
