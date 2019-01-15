package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.pqixing.git.Components
import com.pqixing.help.XmlHelper
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
                .map { historys.remove(it);Pair(it, modules.remove(it)?.introduce ?: "") }, historys
                .map { Pair(it, modules.remove(it)?.introduce ?: "") }, modules
                .map { Pair(it.key, it.value.introduce) })
        dialog.pack()
        dialog.isVisible = true
        dialog.setOkListener {
            WriteCommandAction.runWriteCommandAction(e.project) {
                //保存历史记录
                saveHistory(dialog.history, historyFile)
                saveImport(dialog.select, importFile)
                val dpModel = dialog.dpModel?.trim() ?: ""
                val fileInfo = File(basePath, "ProjectInfo.java")
                if (dpModel != "dpModel" && fileInfo.exists()) {
                    val replace = FileUtils.readText(fileInfo)!!.replace(Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
                    FileUtils.writeText(fileInfo, replace, true)
                }

                val importModel = dialog.importModel
                var import = if (codeRoot.startsWith("..") && importModel == "Import") {
                    addModulesToXml(dialog.select, alls, basePath)
                } else false
                if (!import || importModel == "Sync") {
                    ActionManager.getInstance().getAction("Android.SyncProject")?.actionPerformed(e)
                }
            }
        }
    }

    private fun addModulesToXml(select: List<Pair<String, String>>, alls: HashMap<String, Components>, basePath: String): Boolean {
        val t = "<module fileurl=\"file://###PROJECT_DIR###/IML_PATH\" filepath=\"###PROJECT_DIR###/IML_PATH\" />\n"
        val imls = StringBuilder("<modules>\n<module fileurl=\"file://###PROJECT_DIR###/${project.name}.iml\" filepath=\"###PROJECT_DIR###/${project.name}.iml\" />\n")
        var add = 0
        select.forEach {
            val components = alls[it.first] ?: return@forEach
            val IML_PATH = "${codeRoot}${components.getPath()}/${components.name}.iml"
            if (File(basePath, IML_PATH).exists()) {
                imls.append(t.replace("IML_PATH", IML_PATH))
                add++
            }
        }
        imls.append("</modules>")
        val modules = File(basePath, ".idea/modules.xml")
        val toRegex = "<modules>.*?</modules>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = FileUtils.readText(modules)?.replace(toRegex, imls.toString()) ?: return false
        FileUtils.writeText(modules, result.replace("###", "$"))
        return add == select.size
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

    private fun saveImport(select: MutableList<Pair<String, String>>, importFile: File) {
        val sb = StringBuilder("val include=")
        val end = select.size
        for (i in 0 until select.size) {
            sb.append(select[i].first)
            if (i < end - 1) sb.append("+")
        }
        var i = 0
        val result = Regex("\n *val *include.*").replace(FileUtils.readText(importFile)
                ?: "") {
            if (i++ == 0) "\n" + sb.toString() else "\n//${it.value.replace("\n", "")}"
        }
        FileUtils.writeText(importFile, if (i == 0) "$sb\n$result" else result, true)
    }

    private fun saveHistory(history: List<Pair<String, String>>, historyFile: File) {
        val end = Math.min(15, history.size)
        val sb = StringBuilder()
        for (i in 0 until end) {
            sb.append(history[i].first)
            if (i < end - 1) sb.append(",")
        }
        FileUtils.writeText(historyFile, sb.toString(), true)
    }
}
