package com.pqixing.intellij.actions

import com.intellij.dvcs.ui.DvcsBundle
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.pqixing.git.Components
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.adapter.JListInfo
import com.pqixing.intellij.ui.ImportDialog
import com.pqixing.intellij.utils.Git4IdeHelper
import com.pqixing.tools.FileUtils
import com.pqixing.tools.PropertiesUtils
import git4idea.commands.GitLineHandlerListener
import git4idea.commands.GitStandardProgressAnalyzer
import java.io.File
import java.util.*


class ImportAction : AnAction() {
    private var codeRoot = "main"
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

        val p = PropertiesUtils.readProperties(historyFile)
        val historys = p.getProperty("import")?.split(",")?.toMutableList() ?: mutableListOf()
        val codeRoots = p.getProperty("codeRoot")?.split(",")?.toMutableList() ?: mutableListOf()
        val selects = ArrayList<String>()
        readImport(importFile, selects)
        codeRoots.remove(codeRoot)
        codeRoots.add(0, codeRoot)

        val dialog = ImportDialog(selects
                .map {
                    historys.remove(it);JListInfo(it, modules.remove(it)?.introduce ?: "")
                }, historys
                .map { JListInfo(it, modules.remove(it)?.introduce ?: "") }, modules
                .map { JListInfo(it.key, it.value.introduce) }, codeRoots)
        dialog.pack()
        dialog.isVisible = true
        dialog.setOkListener(object : Task.Backgroundable(project, DvcsBundle.message("cloning.repository", "http://192.168.3.200/android/Document.git")) {
            override fun run(indicator: ProgressIndicator) {
                val dir = File(project.basePath, "../test2")
                if (!dir.exists()) dir.mkdirs()
                val indicator = ProgressManager.getInstance().progressIndicator
                indicator.isIndeterminate = false
                val progressListener = GitStandardProgressAnalyzer.createListener(indicator)
                //                    Git4IdeHelper.getGit().clone(project,dir,"http://192.168.3.200/android/Document.git","Document",progressListener)
                val repo = Git4IdeHelper.getRepo(File(basePath, "../MedicalProject"), this@ImportAction.project);
                val remote = repo.remotes.find { it.name == "origin" } ?: repo.remotes.first()
                var result = Git4IdeHelper.getGit().fetch(repo, remote, Collections.singletonList(GitLineHandlerListener { p0, p1 -> System.out.println(p0 + p1) }))
                if (result.exitCode == 0) {

                    result = Git4IdeHelper.getGit().merge(repo, "${remote.name}/${repo.currentBranch?.name}", emptyList())
                }
                System.out.println("-----------" + result.toString())
            }

            override fun onSuccess() {
            }
        }::queue)
    }

//    private fun checkGit(e: AnActionEvent, items: List<String>, alls: HashMap<String, Components>, success: () -> Unit) {
//        val rootFile = VfsUtil.findFileByIoFile(File(project.basePath), true) ?: return
//        val instance = VcsRepositoryManager.getInstance(project);
//
//        val forFile = instance.getRepositoryForFile(rootFile, false)!!
//        forFile.update()
//
//        instance.removeExternalRepository()
//        instance.addExternalRepository()
//        val rootBranchName = forFile.currentBranchName
//        System.out.println("root bracnch -> $rootBranchName")
//        val repo =  GitRepositoryImpl.getInstance(rootFile,project,false);
//        GitImpl().fetch()
//        val basePath = project.basePath
//        val map = items.map { alls[it]?.rootName }.toSet().map {
//            val rootDir = File(basePath, "$codeRoot$it")
//            if (!rootDir.exists()) Pair(it, "clone")
//            else {
//                val brach = instance.getRepositoryForFile(VfsUtil.findFileByIoFile(File(project.basePath), false)!!, false)?.currentBranchName
//                if (brach == rootBranchName) Pair(it, "normal")
//                else Pair(it, "ckeckout")
//            }
//        }
//        System.out.println(map)
//
//    }

    private fun onImport(e: AnActionEvent, dialog: ImportDialog, historyFile: File, importFile: File, basePath: String, selects: ArrayList<String>, alls: HashMap<String, Components>, codeRoots: MutableList<String>) {
        WriteCommandAction.runWriteCommandAction(project) {
            var curRoot = dialog.codeRoot.selectedItem?.toString()?.trim()
            if (curRoot?.isNotEmpty() != true) curRoot = "main"
            //保存历史记录
            saveHistory(dialog.historyModel.selectItems, historyFile, curRoot, codeRoots)
            saveImport(dialog.selctModel.selectItems, importFile, curRoot)
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
            val pathIml = File(basePath, "../$codeRoot${components.getPath()}/${components.name}.iml")
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
                if (codeRoot.isEmpty()) codeRoot = "main"
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

    private fun saveImport(select: MutableList<JListInfo>, importFile: File, curRoot: String) {
        val sb = StringBuilder("val include=")
        val end = select.size
        for (i in 0 until select.size) {
            sb.append(select[i].title)
            if (i < end - 1) sb.append("+")
        }
        var result = FileUtils.readText(importFile) ?: ""
        for (m in mapOf("include" to sb.toString(), "codeRoot" to "val codeRoot=$curRoot")) {
            var i = 0
            result = Regex("\n *val *${m.key}.*").replace(result) {
                if (i++ == 0) "\n" + m.value else "\n//${it.value.replace("\n", "")}"
            }
            if (i == 0) result = m.value + "\n" + result
        }

        FileUtils.writeText(importFile, result, true)
    }

    private fun saveHistory(history: List<JListInfo>, historyFile: File, curRoot: String, codeRoots: MutableList<String>) {
        codeRoots.remove(curRoot)
        codeRoots.add(0, curRoot)
        val end = Math.min(15, history.size)
        val sb = StringBuilder("import=")
        for (i in 0 until end) {
            sb.append(history[i].title)
            if (i < end - 1) sb.append(",")
        }
        sb.append("\ncodeRoot=")
        for (c in codeRoots) {
            sb.append("$c,")
        }
        sb.deleteCharAt(sb.length - 1)
        FileUtils.writeText(historyFile, sb.toString(), true)
    }
}
