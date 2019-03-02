package com.pqixing.intellij.actions

import com.android.internal.R.attr.action
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.messages.impl.Message
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.ImportDialog
import com.pqixing.model.ProjectXmlModel
import com.pqixing.tools.FileUtils
import groovy.lang.GroovyClassLoader
import java.io.File


class ImportAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXmlFile = File(basePath, "templet/project.xml")
        val configFile = File(basePath, "Config.java")
        if(!projectXmlFile.exists()||!configFile.exists()){
            Messages.showMessageDialog("Project or Config file not exists!!","Miss File",null)
            return
        }
        val projectXml = XmlHelper.parseProjectXml(projectXmlFile)
        val clazz = GroovyClassLoader().parseClass(configFile)
        val newInstance = clazz.newInstance()
        val includes = clazz.getField("include").get(newInstance).toString()
        val codeRoot = clazz.getField("codeRoot").get(newInstance).toString()
        val dependentModel = clazz.getField("dependentModel").get(newInstance).toString()


        val smartSet = includes.replace("+", ",").split(",").mapNotNull { if (it.isEmpty()) null else it.trim() }.toSet()
        val sortedBy = projectXml.allSubModules().map { JListInfo(it.name, it.introduce, 0, smartSet.contains(it.name)) }
        val spec = StringBuilder()
        smartSet.filter { it.contains("#") }.forEach {
            if (it.isNotEmpty()) spec.append(it).append(",")
        }
        val dialog = ImportDialog(sortedBy.filter { it.select }, sortedBy.filter { !it.select }, spec.toString(), codeRoot, dependentModel)
        dialog.pack()
        dialog.isVisible = true
        val action = Runnable {
            saveConfig(configFile, dialog)

            val import = dialog.importModel == "Import"
                    && importByIde(smartSet, dialog.selctModel.selectItems
                    .map { Pair(it.title, getImlPath(codeRoot, projectXml, it.title)) }
                    .toMap(mutableMapOf()))
//                    && dependentModel == dialog.dpModel//如果依赖方式改变了,需要同步处理
                    && smartSet.toString() == dialog.specificInclude.text.trim()//如果有特殊导入有改变,需要同步

            //如果快速导入不成功,则,同步一次
            if (!import) ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)
        }
        dialog.btnConfig.addActionListener {
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(configFile, false)!!, true)
            dialog.dispose()
        }
        dialog.btnXml.addActionListener {
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(projectXmlFile, false)!!, true)
        }
        dialog.setOkListener { ProgressManager.getInstance().executeProcessUnderProgress(action,null) }
    }

    private fun getImlPath(codeRoot: String, projectXml: ProjectXmlModel, title: String) = "$basePath/$codeRoot/${projectXml.findSubModuleByName(title)?.path}/$title.iml"

    private fun saveConfig(configgFile: File, dialog: ImportDialog) {
        val dpModel = dialog.dpModel?.trim() ?: ""
        val codeRoot = dialog.codeRootStr.trim()
        val includes = dialog.selctModel.selectItems.map { it.title }

        val includeBuilder = StringBuilder()
        includes.forEach { if (it.isNotEmpty()) includeBuilder.append(it).append(",") }
        includeBuilder.append(dialog.specificInclude.text)
        val iss = includeBuilder.toString()
        var result = configgFile.readText()
        result = result.replace(Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
        result = result.replace(Regex("String *codeRoot *=.*;"), "String codeRoot = \"$codeRoot\";")
        result = result.replace(Regex("String *include *=.*;"), "String include = \"$iss\";")
        FileUtils.writeText(configgFile, result, true)
    }

    /**
     * 直接通过ide进行导入
     */
    private fun importByIde(before: Set<String>, includes: MutableMap<String, String>): Boolean {
        val manager = ModuleManager.getInstance(project)
        var fail = 0

        val set = before.toMutableSet()
        includes.forEach { if (!set.remove(it.key)) fail += loadModule(manager, it.value) }

        set.filter { !it.contains("#") }.forEach {
            manager.disposeModule(manager.findModuleByName(it) ?: return@forEach)
        }
        return fail == 0
    }

    /**
     * 加载模块
     */
    private fun loadModule(manager: ModuleManager, filePath: String): Int {
        if (!File(filePath).exists()) return 0
        return try {
            manager.loadModule(filePath);0
        } catch (e: Exception) {
            1
        }
    }
}
