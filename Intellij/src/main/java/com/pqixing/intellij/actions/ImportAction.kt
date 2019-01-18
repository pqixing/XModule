package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.pqixing.git.Components
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.ImportDialog
import com.pqixing.tools.FileUtils
import java.io.File


class ImportAction : AnAction() {
    private var codeRoot = "../"
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        val basePath = e.project?.basePath ?: return
        project = e.project!!
        val alls = HashMap<String, Components>().apply {
            XmlHelper.parseProjectXml(File(basePath, "manager/project.xml"), this)
        }
        val modules = alls.toMutableMap()
        val historyFile = File(basePath, ".idea/caches/import.txt")
        val importFile = File(basePath, "import.kt")

        val historys = FileUtils.readText(historyFile)?.split(",")?.toMutableList()
                ?: mutableListOf()
        val selects = ArrayList<String>()
        readImport(importFile, selects)
        val dialog = ImportDialog(selects
                .map {
                    historys.remove(it);JListInfo(it, modules.remove(it)?.introduce ?: "")
                }, historys
                .map { JListInfo(it, modules.remove(it)?.introduce ?: "") }, modules
                .map { JListInfo(it.key, it.value.introduce) })
        dialog.pack()
        dialog.isVisible = true
        dialog.setOkListener {
            WriteCommandAction.runWriteCommandAction(e.project) {
                //保存历史记录
                saveHistory(dialog.historyModel.selectItems, historyFile)
                saveImport(dialog.selctModel.selectItems, importFile)
                val dpModel = dialog.dpModel?.trim() ?: ""
                val fileInfo = File(basePath, "ProjectInfo.java")
                if (dpModel != "dpModel" && fileInfo.exists()) {
                    val replace = FileUtils.readText(fileInfo)!!.replace(Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
                    FileUtils.writeText(fileInfo, replace, true)
                }

                val importModel = dialog.importModel
                var import = if (codeRoot.startsWith("..") && importModel == "Import") {
                    importByIde(selects, dialog.selctModel.selectItems.map { it.title }, alls, basePath)
                } else false
                if (!import || importModel == "Sync") {
                    ActionManager.getInstance().getAction("Android.SyncProject")?.actionPerformed(e)
                }
            }
        }
    }

    private fun importByIde(oldSecelt: List<String>, newSelect: List<String>, alls: HashMap<String, Components>, basePath: String): Boolean {
        val manager = ModuleManager.getInstance(project);
        val addModule = newSelect.toMutableList().apply { removeAll(oldSecelt) }
        val removeModule = oldSecelt.toMutableList().apply { removeAll(newSelect) }
        removeModule.forEach {
            val m = manager.findModuleByName(it) ?: return@forEach
            manager.disposeModule(m)
        }
        var add = 0
        addModule.forEach {
            val components = alls[it] ?: return@forEach
            val pathIml = File(basePath, "${codeRoot}${components.getPath()}/${components.name}.iml")
            if (pathIml.exists()) {
                try {
                    manager.loadModule(pathIml.absolutePath)
                    add++
                } finally {
                }
            }
        }
        return add == addModule.size
    }

    private fun readImport(importFile: File, selects: ArrayList<String>) {
        for (line in FileUtils.readText(importFile)?.lines() ?: emptyList()) {
            if (line.trim().startsWith("//END")) break
            val map = line.replace(Regex("//.*"), "").split("=")
            if (map.size < 2) continue
            val key = map[0].replace("var ", "").replace("val ", "").replace("\"", "").trim()
            if (key == "codeRoot") {
                codeRoot = map[1].replace("\"", "").trim()
                if (codeRoot.isEmpty()) codeRoot = "../"
                if (!codeRoot.endsWith("/")) codeRoot = "$codeRoot/"
            }
            if (key != "include") continue
            map[1].replace("+", ",")
                    .replace("\"", "")
                    .split(",").map { it.trim() }.forEach {
                        if (it.isEmpty() || selects.contains(it)) return@forEach
                        selects.add(it)
                    }
        }
    }

    private fun saveImport(select: MutableList<JListInfo>, importFile: File) {
        val sb = StringBuilder("val include=")
        val end = select.size
        for (i in 0 until select.size) {
            sb.append(select[i].title)
            if (i < end - 1) sb.append("+")
        }
        var i = 0
        val result = Regex("\n *val *include.*").replace(FileUtils.readText(importFile)
                ?: "") {
            if (i++ == 0) "\n" + sb.toString() else "\n//${it.value.replace("\n", "")}"
        }
        FileUtils.writeText(importFile, if (i == 0) "$sb\n$result" else result, true)
    }

    private fun saveHistory(history: List<JListInfo>, historyFile: File) {
        val end = Math.min(15, history.size)
        val sb = StringBuilder()
        for (i in 0 until end) {
            sb.append(history[i].title)
            if (i < end - 1) sb.append(",")
        }
        FileUtils.writeText(historyFile, sb.toString(), true)
    }
}
