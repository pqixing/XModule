package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.SubModule
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.manager.ProjectManager.codeRootDir
import com.pqixing.modularization.manager.ProjectManager.projectXml
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

object ProjectManager : OnClear {
    override fun start() {
        projectXml = XmlHelper.parseProjectXml(FileManager.getProjectXml())
        projectRoot = ManagerPlugin.getPlugin().projectDir
        codeRootDir = File(ManagerPlugin.getPlugin().rootDir, ManagerPlugin.getExtends().config.codeRoot)
    }

    init {
        BasePlugin.addClearLister(this)
        start()
    }

    override fun clear() {
    }

    lateinit var projectXml: ProjectXmlModel

    lateinit var projectRoot: File

    lateinit var codeRootDir: File

    fun findSubModuleByName(name: String) = projectXml.findSubModuleByName(name)

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project): SubModule? {
        val extends = ManagerPlugin.getExtends()
        val info = extends.config
        val buildDir = info.buildDir.toString().trim()
        //不在配置文件的git工程，不进行管理
        val subModule = projectXml.findSubModuleByName(project.name) ?: return null
        val apiModule = subModule.isApiModule()
        //重新设置build 目录
        project.buildDir = File(project.projectDir, "build/" + (if (buildDir.isEmpty()) "default" else buildDir))

        if (subModule.hasCheck) return subModule

        val projectDir = File(codeRootDir, subModule.project.name)

        val docRepoBranch = extends.docRepoBranch
        var mBranch = subModule.project.branch
        if (mBranch.isEmpty()) if (GitUtils.isGitDir(projectDir)) Git.open(projectDir)
        else {
            FileUtils.delete(projectDir)
            GitUtils.clone(subModule.project.url, projectDir)
        }?.apply {
            mBranch = this.repository.branch
            subModule.project.branch = mBranch
            GitUtils.close(this)
        }
        if (mBranch != docRepoBranch) {
            Tools.println("${subModule.name} branch is $mBranch , do not match doc branch $docRepoBranch")
        }
        //如果是Api工程,检查基础模块在不在
        if (apiModule) {
            val moduleDir = File(codeRootDir, subModule.path)
            with(File(moduleDir, "build.gradle")) {
                if (!exists()) FileUtils.writeText(this, "apply plugin: 'com.module.android'\n endConfig()\n ")
            }
            val name = TextUtils.numOrLetter(project.name)
            val packageName = "${ManagerPlugin.getExtends().groupName.replace(".", "/")}/auto/router/$name"
            val className = "${TextUtils.firstUp(name)}Paths"
            with(File(moduleDir, "java/$packageName/$className.java")) {
                if (!exists()) FileUtils.writeText(this, "package ${packageName.replace("/", ".")};\nfinal class $className {}")
            }
            with(File(moduleDir, "resources/values/strings.xml")) {
                if (!exists()) FileUtils.writeText(this, "<resources></resources>")

            }
            //写入空清单文件
            with(File(moduleDir, "AndroidManifest.xml")) {
                if (!exists()) {
                    val emptyManifest = (FileUtils.readText(File(projectRoot, "templet/android/Empty_AndroidManifest.xml"))
                            ?: "").replace("[groupName]", extends.groupName).replace("[projectName]", subModule.name)
                    FileUtils.writeText(this, emptyManifest)
                }
            }
        }
        subModule.hasCheck = true
        return subModule
    }
}
