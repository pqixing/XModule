package com.pqixing.modularization.manager

import com.pqixing.Config
import com.pqixing.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.ProjectXmlModel
import com.pqixing.model.SubModule
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.interfaces.OnClear
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.tools.FileUtils
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

object ProjectManager : OnClear {
    init {
        BasePlugin.addClearLister(this)
    }

    override fun clear() {
    }

    var projectXml: ProjectXmlModel = XmlHelper.parseProjectXml(FileManager.getProjectXml())

    var projectRoot: File = ManagerPlugin.getPlugin().projectDir

    var codeRootDir: File = File("_empty")
        get() {
            if (field.name == "_empty") {
                field = File(ManagerPlugin.getPlugin().rootDir, ManagerPlugin.getExtends().config.codeRoot)
            }
            return field
        }

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project): SubModule? {
        val extends = ManagerPlugin.getExtends()
        val info = extends.config
        val buildDir = info.buildDir.toString().trim()
        //重新设置build 目录
        project.buildDir = File(project.buildDir, if (buildDir.isEmpty()) "default" else buildDir)

        //不在配置文件的git工程，不进行管理
        val subModule = projectXml.findSubModuleByName(project.name) ?: return null

        val projectDir = File(codeRootDir, subModule.project.name)

        val docRepoBranch = extends.docRepoBranch
        var mBranch = subModule.project.branch
        if (mBranch.isEmpty()) if (GitUtils.isGitDir(projectDir)) Git.open(projectDir)
        else {
            GitUtils.clone(subModule.project.url, projectDir)
        }?.apply {
            mBranch = this.repository.branch
            subModule.project.branch = mBranch
            close()
        }
        if (mBranch != docRepoBranch) {
            Tools.println("${subModule.name} branch is $mBranch , do not match doc branch $docRepoBranch")
        }
        //如果是Api工程,检查基础模块在不在
        if (subModule.isApiModule()) {
            val moduleDir = File(codeRootDir, subModule.path)
            with(File(moduleDir, "build.gradle")) {
                if (!exists()) FileUtils.writeText(this, "apply plugin: 'com.module.android' \n apply from: \"\$gradles/com.module.ktolin.gradle\"")
            }
            with(File(moduleDir, "AndroidManifest.xml")) {
                if (!exists()) FileUtils.writeText(this, "apply from: \"\$gradles/com.module.ktolin.gradle\"")
            }
            with(File(moduleDir, "java")) {
                if (!exists()) this.mkdir()
            }
            with(File(moduleDir, "resources")) {
                if (!exists()) this.mkdir()
            }
            //写入空清单文件
            with(File(moduleDir, "AndroidManifest.xml")) {
                if (exists()) {
                    val emptyManifest = (FileUtils.readText(File(projectRoot, "templet/android/Empty_AndroidManifest.xml"))
                            ?: "").replace("[groupName]", extends.groupName).replace("[projectName]", subModule.name)
                    FileUtils.writeText(this, emptyManifest)
                }
            }
        }
        return subModule
    }
}
