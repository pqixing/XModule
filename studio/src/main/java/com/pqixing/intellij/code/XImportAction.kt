package com.pqixing.intellij.code

import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.getProjectSystem
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.Key
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XApp
import com.pqixing.intellij.XApp.getOrElse
import com.pqixing.intellij.XApp.getSp
import com.pqixing.intellij.XApp.putSp
import com.pqixing.intellij.common.XEventAction
import com.pqixing.intellij.git.uitils.GitHelper
import com.pqixing.intellij.ui.weight.MyMenuItem
import com.pqixing.intellij.ui.weight.XItem
import com.pqixing.intellij.ui.weight.XModuleDialog
import com.pqixing.intellij.ui.weight.showPop
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import git4idea.commands.GitLineHandlerListener
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.util.*
import javax.swing.*


open class XImportAction : XEventAction<XImportDialog>()
class XImportDialog(e: AnActionEvent) : XModuleDialog(e) {
    val VCS_KEY = "Vcs"
    private lateinit var pTop: JPanel
    private lateinit var tvSearch: JTextField
    private lateinit var tvCodeRoot: JTextField
    private lateinit var btnFile: JButton
    private lateinit var btnImport: JButton
    private lateinit var cbDepend: JComboBox<String>
    override fun getAllCheckBox(): JCheckBox = JCheckBox(VCS_KEY, null, VCS_KEY.getSp("Y", project) == "Y")
    override fun doOnAllChange(selected: Boolean) = VCS_KEY.putSp(selected.getOrElse("Y", "N"), project)
    override fun getTitleStr(): String = "Import"
    override fun createNorthPanel(): JComponent? = pTop

    init {
        XApp.runAsyn { initList() }
        cbDepend.selectedItem = config.dependentModel
        tvCodeRoot.text = config.codeRoot
        btnFile.addActionListener { openFile() }
        btnImport.addActionListener { showImportPop(btnImport, adapter.datas().filter { it.select }, false) }
        tvSearch.registerKeyboardAction({ tvSearch.text = "" }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)
        tvSearch.addKeyListener(object : KeyAdapter() {

            override fun keyReleased(e: KeyEvent?) {
                val keyCode: Int = e?.keyCode ?: return
                XApp.log("keyReleased -> $keyCode , ${e.keyChar} ,${e.extendedKeyCode} ,${e.source}")
                val key: String = tvSearch.text.trim()
                when (keyCode) {
                    KeyEvent.VK_DOWN, KeyEvent.VK_CONTROL -> showImportPop(tvSearch, adapter.datas().filter { match(key, listOf(it.title, it.content, it.tag)) }, true)
                }
                for (it in adapter.datas()) {
                    it.visible = key.isEmpty() || match(key, listOf(it.title, it.content, it.tag))
                }
            }
        })
    }

    private fun openFile() {
        btnFile.showPop(listOf("manifest", "config"), Point(0, btnFile.height + 10)) {
            when (it.data) {
                "manifest" -> XApp.openFile(project, XmlHelper.fileManifest(basePath))
                "config" -> XApp.openFile(project, XmlHelper.fileConfig(basePath))
            }
        }
    }

    fun showImportPop(c: JComponent, datas: List<XItem>, select: Boolean, point: Point = Point(0, c.height + 10)) {
        val click = { i: MyMenuItem<XItem> -> i.data?.let { it.select = !it.select } ?: Unit }
        c.showPop(datas.map { m -> MyMenuItem(m.title, m, select && m.select, click) }, point)
    }

    fun match(searchKey: String, matchs: List<String>): Boolean {
        if (searchKey.trim().isEmpty()) return true
        val key = searchKey.trim().toLowerCase(Locale.CHINA)
        for (m in matchs) {
            var k = 0
            var l = -1
            val line = m.toLowerCase(Locale.CHINA)
            while (++l < line.length) if (key[k] == line[l] && ++k == key.length) return true
        }
        return false
    }

    private fun count() {
        btnImport.text = "Import : ${adapter.datas().filter { it.select }.size}"
    }

    override fun doOKAction() {
        super.doOKAction()

        XApp.runAsyn { indictor ->
            indictor.text = "Start Import"

            val imports = adapter.datas().filter { it.select }.map { it.title }.also { saveConfig(it) }

            val codePath = File(basePath, config.codeRoot).canonicalPath
            //下载代码
            val projects = manifest!!.allModules().filter { imports.contains(it.name) }.map { it.project }.toSet()
            for (p in projects) {
                val dir = File(codePath, p.path)
                if (GitUtil.isGitRoot(dir)) continue
                FileUtils.delete(dir)
                indictor.text = "Start Clone ${p.url} -> ${config.codeRoot}"
                //下载master分支
                GitHelper.clone(project, dir, p.url, object : GitLineHandlerListener {
                    override fun onLineAvailable(line: String?, outputType: Key<*>?) {
                        indictor.text = "Clone ${p.url} : $line"
                    }
                })
            }

            indictor.text = "Start Sync Code"
            //如果快速导入不成功,则,同步一次
            //ActionManager.getInstance().getAction("ExternalSystem.RefreshAllProjects").actionPerformed(e)
            ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)

            indictor.text = "Start Sync Vcs"
            XApp.syncVcs(project, cbAll.isSelected)
        }

    }

    private fun saveConfig(imports: Collection<String>) = XApp.invokeWrite {
        config.dependentModel = cbDepend.selectedItem?.toString()?.trim() ?: ""
        config.codeRoot = tvCodeRoot.text.trim()
        config.include = imports.filter { it.isNotEmpty() }.joinToString(",")
        XmlHelper.saveConfig(basePath, config)
    }


    override fun getPreferredFocusedComponent(): JComponent? = tvSearch

    private fun initList() {
        val source = config.include.split(",").mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } }.toSet()
        val import = XmlHelper.parseInclude(manifest!!, source)
        val allItems = manifest.allModules().map { m ->
            val item = XItem()
            item.select = import.contains(m.name)
            item.title = m.name
            item.content = "${m.path}  -- ${m.desc}"
            item.tag = m.type
            item.right = { c, e -> c.showPop(listOf("show depends", "import depends", "remove depends"), Point(e.x, e.y)) { onRightPopClick(c, Point(e.x, e.y), item, it) } }
            item.cbSelect.addItemListener { count() }
            item
        }.sortedBy { !it.select }
        adapter.set(allItems)
        count()
    }

    /**
     * 点击Item右键
     */
    private fun onRightPopClick(c: JComponent, point: Point, item: XItem, menu: MyMenuItem<String>) {
        val module = manifest?.findModule(item.title) ?: return
        val dpItems = module.allCompiles(true)?.let { l -> adapter.datas().filter { l.contains(it.title) } }

        when (menu.label) {
            "show depends" -> showImportPop(c, dpItems, true, point)
            "import depends" -> dpItems.forEach { it.select = true }
            "remove depends" -> dpItems.forEach { it.select = false }
        }
    }
}


