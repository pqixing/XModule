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

    var codeRootDir: File = File(".")
        get() {
            if (field.name != "src") {
                field = File(ManagerPlugin.getPlugin().rootDir, "src")
            }
            return field
        }

    /**
     * 检查每个子工程的状态，分支信息等
     */
    fun checkProject(project: Project, info: Config): SubModule? {
        val buildDir = info.buildDir.toString().trim()
        //重新设置build 目录
        project.buildDir = File(project.buildDir, if (buildDir.isEmpty()) "default" else buildDir)

        //不在配置文件的git工程，不进行管理
        val subModule = projectXml.findSubModuleByName(project.name) ?: return null

        val projectDir = File(codeRootDir, subModule.project.name)
        val moduleDir = File(codeRootDir, subModule.path)

        val docRepoBranch = ManagerPlugin.getExtends().docRepoBranch
        var mBranch = subModule.project.branch
        if (mBranch.isEmpty()) if (!moduleDir.exists() || !checkRootDir(moduleDir, projectDir)) {//下载工程
            GitUtils.clone(subModule.project.url, projectDir)
        } else {
            Git.open(projectDir)
        }?.apply {
            mBranch = this.repository.branch
            subModule.project.branch = mBranch
            close()
        }
        if (mBranch != docRepoBranch) {
            Tools.println("${subModule.name} branch is $mBranch , do not match doc branch $docRepoBranch")
        }
        return subModule
    }

    fun checkRootDir(moduleDir: File, projectDir: File): Boolean {
        //如果根目录不是git目录,先删除
        if (!GitUtils.isGitDir(projectDir) || moduleDir.listFiles().size < 3) {
            FileUtils.delete(projectDir)
            return false
        }
        return true
    }
}
