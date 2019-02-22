package com.pqixing.intellij.actions

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
import com.pqixing.tools.PropertiesUtils
import java.io.File
import java.util.*


class ImportAction : AnAction() {
    lateinit var project: Project
    lateinit var basePath: String
    override fun actionPerformed(e: AnActionEvent) {
        project = e.project ?: return
        basePath = project.basePath ?: return

        val alls = HashMap<String, Components>().apply {
            XmlHelper.parseProjectXml(File(basePath, "manager/project.xml"), this)
        }
        val modules = alls.toMutableMap()
        val historyFile = File(basePath, ".idea/caches/import.txt")
        val importFile = File(basePath, "import.kt")

        val p = PropertiesUtils.readProperties(historyFile)
        val historys = p.getProperty("import")?.split(",")?.toMutableList() ?: mutableListOf()
        val codeRoots = p.getProperty("codeRoot")?.split(",")?.toMutableList() ?: mutableListOf()
        val selects = ArrayList<String>()

        val codeRoot = readImport(importFile, selects)
        codeRoots.remove(codeRoot)
        codeRoots.add(0, codeRoot)

        val dialog = ImportDialog(
                selects.map { JListInfo(it, modules.remove(it)?.introduce ?: "") },
                modules.map { JListInfo(it.key, it.value.introduce) }.sortedBy { historys.indexOf(it.title) }
                , codeRoots)

        dialog.pack()
        dialog.isVisible = true
        val action = Runnable {
            val curRoot = dialog.codeRootStr
            codeRoots.remove(curRoot)
            codeRoots.add(0, curRoot)
            val select = dialog.selctModel.selectItems.onEach {
                historys.remove(it.title);
                historys.add(0, it.title)
            }
            //保存历史记录
            saveHistory(historyFile, historys, codeRoots)
            //保存操作结果
            saveImport(importFile, select, curRoot)
            saveDpModel(dialog, basePath)

            val import = dialog.importModel == "Import"
                    && importByIde(selects, select.mapNotNull { alls[it.title] },codeRoot)

            //如果快速导入不成功,则,同步一次
            if (!import) RunAction("Android.SyncProject").actionPerformed(e)
        }
        dialog.setOkListener { WriteCommandAction.runWriteCommandAction(project, action) }
    }

    private fun saveDpModel(dialog: ImportDialog, basePath: String) {
        val dpModel = dialog.dpModel?.trim() ?: ""
        val fileInfo = File(basePath, "templet.java")
        if (dpModel != "dpModel" && fileInfo.exists()) {
            val replace = FileUtils.readText(fileInfo)!!.replace(
                    Regex("String *dependentModel *=.*;"), "String dependentModel = \"$dpModel\";")
            FileUtils.writeText(fileInfo, replace, true)
        }
    }

    private fun importByIde(oldSelect: List<String>, newSelect: List<Components>,codeRoot:String): Boolean {
        val manager = ModuleManager.getInstance(project);

        val addModule = newSelect.filter { !oldSelect.contains(it.name) }
        oldSelect.forEach { o ->
            val f = newSelect.find { o == it.name }
            val m = manager.findModuleByName(o)
            if (m == null || f != null) return@forEach
            manager.disposeModule(m)
        }
        var add = 0
        addModule.forEach { components ->
            val pathIml = File(basePath, "../$codeRoot${components.getPath()}/${components.name}.iml")
            if (pathIml.exists()) try {
                manager.loadModule(pathIml.absolutePath)
                add++
            } finally {
            }
        }
        return add == addModule.size
    }

    private fun readImport(importFile: File, selects: ArrayList<String>): String {
        var codeRoot = ""
        for (line in FileUtils.readText(importFile)?.lines() ?: emptyList()) {
            if (line.trim().startsWith("//END")) break
            val map = line.replace(Regex("//.*"), "").split("=")
            if (map.size < 2) continue
            val key = map[0].replace("var ", "").replace("val ", "").replace("\"", "").trim()
            when (key) {
                "codeRoot" -> codeRoot = map[1].replace("\"", "").trim()
                "include" -> map[1].replace("+", ",").replace("\"", "")
                        .split(",").forEach {
                            val i = it.trim()
                            if (!selects.contains(i)) selects.add(i)
                        }
            }
        }
        return if (codeRoot.isEmpty()) "main" else codeRoot
    }

    private fun saveImport(importFile: File, select: MutableList<JListInfo>, curRoot: String) {
        val sb = StringBuilder("val include=")
        val end = select.size
        for (i in 0 until select.size) {
            sb.append(select[i].title)
            if (i < end - 1) sb.append("+")
        }
        var result = FileUtils.readText(importFile) ?: ""
        var j = 0
        for (m in mapOf("include" to sb.toString(), "codeRoot" to "val codeRoot=$curRoot")) {
            var i = 0
            result = Regex("\n *val *${m.key}.*").replace(result) {
                if (i++ == 0) "\n" + m.value else "\n//${it.value.replace("\n", "")}"
            }
            j += i
            if (i == 0) result = m.value + "\n" + result
        }

        if (j != 0) FileUtils.writeText(importFile, result, false)
    }

    private fun saveHistory(historyFile: File, historys: MutableList<String>, codeRoots: MutableList<String>) {

        val sb = StringBuilder("import= ")
        historys.forEach { sb.append("$it,") }
        sb.deleteCharAt(sb.length - 1)

        sb.append("\ncodeRoot=")
        for (c in codeRoots) {
            sb.append("$c,")
        }
        sb.deleteCharAt(sb.length - 1)
        FileUtils.writeText(historyFile, sb.toString(), true)
    }
}
