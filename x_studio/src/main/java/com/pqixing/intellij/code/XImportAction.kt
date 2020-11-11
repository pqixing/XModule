package com.pqixing.intellij.code

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.pqixing.Config
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XApp
import com.pqixing.intellij.XApp.getOrElse
import com.pqixing.intellij.XApp.getSp
import com.pqixing.intellij.XApp.putSp
import com.pqixing.intellij.XGroup
import com.pqixing.intellij.actions.XEventAction
import com.pqixing.intellij.git.uitils.GitHelper
import com.pqixing.intellij.ui.NewImportDialog
import com.pqixing.intellij.ui.adapter.JListInfo
import com.pqixing.intellij.ui.weight.MyMenuItem
import com.pqixing.intellij.ui.weight.XItem
import com.pqixing.intellij.ui.weight.XModuleDialog
import com.pqixing.intellij.ui.weight.showPop
import com.pqixing.intellij.utils.UiUtils.realName
import com.pqixing.tools.FileUtils
import git4idea.GitUtil
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*


open class XImportAction : XEventAction<XImportDialog>() {
    lateinit var project: Project
    lateinit var basePath: String

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = XGroup.isBasic(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (true) return super.actionPerformed(e)
        project = e.project ?: return
        basePath = project.basePath ?: return
        val projectXml = XmlHelper.loadManifest(basePath)
        if (projectXml == null) {
            Messages.showMessageDialog("Project or Config file not exists!!", "Miss File", null)
            return
        }
        val config = XmlHelper.loadConfig(basePath)
        val includes = config.include
        val codeRoot = config.codeRoot
        val dependentModel = config.dependentModel

        val imports = includes.replace("+", ",").split(",").mapNotNull { if (it.trim().isEmpty()) null else it.trim() }.toList()
        val infos = projectXml.allModules().filter { it.attach == null }.map { JListInfo(it.path.substringBeforeLast("/") + "/" + it.name, "${it.desc} - ${it.type.substring(0, 3)} ") }.toMutableList()

        val repo = GitHelper.getRepo(File(basePath, EnvKeys.BASIC), project)

        val dialog = NewImportDialog(project, imports.toMutableList(), infos, dependentModel, codeRoot)
        dialog.pack()
        dialog.isVisible = true
        val importTask = { indicator: ProgressIndicator ->
            saveConfig(config, dialog)
            val allIncludes = XmlHelper.parseInclude(projectXml, dialog.imports.toSet())

            val codePath = File(basePath, dialog.codeRootStr).canonicalPath
            //下载代码
            val gitPaths = projectXml.allModules().filter { allIncludes.contains(it.name) }
                    .map { it.project }.toSet().map { File(codePath, it.path) to it.url }.toMap().toMutableMap()

            gitPaths.filter { !GitUtil.isGitRoot(it.key) }.forEach {
                indicator.text = "Start Clone... ${it.value} "

                FileUtils.delete(it.key)
                //下载master分支
                GitHelper.clone(project, it.key, it.value)
            }
//                    如果快速导入不成功,则,同步一次
            ActionManager.getInstance().getAction("Android.SyncProject").actionPerformed(e)

            disposeModule(project, allIncludes, dialog.codeRootStr == codeRoot)
            //添加basic的地址
            XApp.invoke { syncVcs(gitPaths.keys.toMutableSet().also { it.add(File(basePath, EnvKeys.BASIC)) }, dialog.syncVcs(), project) }
        }
        dialog.btnConfig.addActionListener {
            dialog.dispose()
            XApp.openFile(project, XmlHelper.fileConfig(basePath))
        }
        dialog.btnProjectXml.addActionListener {
            dialog.dispose()
            XApp.openFile(project, XmlHelper.fileManifest(basePath))
        }
        dialog.setOnOk { XApp.runAsyn(project, "Start Import", importTask) }
    }

    private fun syncVcs(dirs: MutableSet<File>, syncVcs: Boolean, project: Project) {
        if (syncVcs) {
            //根据导入的CodeRoot目录,自动更改AS的版本管理
            val pVcs: ProjectLevelVcsManagerImpl = ProjectLevelVcsManagerImpl.getInstance(project) as ProjectLevelVcsManagerImpl
            pVcs.directoryMappings = dirs.filter { GitUtil.isGitRoot(it) }.map { VcsDirectoryMapping(it.absolutePath, "Git") }
            pVcs.notifyDirectoryMappingChanged()
        } else {
            /**
             * 所有代码的跟目录
             * 对比一下,当前导入的所有工程,是否都在version管理中,如果没有,提示用户进行管理
             */
            val controlPaths = VcsRepositoryManager.getInstance(project).repositories.map { it.presentableUrl }
            dirs.removeIf { controlPaths.contains(it.absolutePath) }
            if (dirs.isNotEmpty())
                Messages.showMessageDialog("Those project had import but not in Version Control\n ${dirs.joinToString { "\n" + it }} \n Please check Setting -> Version Control After Sync!!", "Miss Vcs Control", null)
        }
    }


    /**
     * 直接通过ide进行导入
     */
    private fun disposeModule(project: Project, allIncludes: MutableSet<String>, codeRootChange: Boolean) = ApplicationManager.getApplication().invokeLater {

        val projectName = project.name.trim().replace(" ", "")
        val manager = ModuleManager.getInstance(project)
        manager.modules.forEach { m ->
            if (projectName == m.name) return@forEach
            if (codeRootChange || !allIncludes.contains(m.realName())) kotlin.runCatching { manager.disposeModule(m) }
        }
    }

    private fun saveConfig(config: Config, dialog: NewImportDialog) = XApp.invokeWrite {
        config.dependentModel = dialog.dpModel?.trim() ?: ""
        config.codeRoot = dialog.codeRootStr.trim()
        config.include = dialog.imports.filter { it.isNotEmpty() }.joinToString(",")
        XmlHelper.saveConfig(basePath, config)
    }
}

class XImportDialog(e: AnActionEvent) : XModuleDialog(e) {
    val VCS_KEY = "Vcs"
    private lateinit var pTop: JPanel
    private lateinit var tvSearch: JTextField
    private lateinit var tvCodeRoot: JTextField
    private lateinit var btnFile: JButton
    private lateinit var btnImport: JButton
    private lateinit var cbDepend: JComboBox<String>
    override fun getAllCheckBox(): JCheckBox = JCheckBox(VCS_KEY, null, VCS_KEY.getSp("Y") == "Y")
    override fun doOnAllChange(selected: Boolean) = VCS_KEY.putSp(selected.getOrElse("Y", "N"))
    override fun getTitleStr(): String = "Import"
    override fun createNorthPanel(): JComponent? = pTop

    init {
        XApp.runAsyn { initList() }
        cbDepend.selectedItem = config.dependentModel
        tvCodeRoot.text = config.codeRoot
        btnFile.addActionListener {
            btnFile.showPop(listOf("manifest", "config"), Point(0, btnFile.height + 10)) {
                when (it.data) {
                    "manifest" -> XApp.openFile(project, XmlHelper.fileManifest(basePath))
                    "config" -> XApp.openFile(project, XmlHelper.fileConfig(basePath))
                }
            }
        }
        btnImport.addActionListener { showImportPop(btnImport, adapter.datas().filter { it.select }, false) }

        tvSearch.addKeyListener(object : KeyAdapter() {
            var preCtrol = false
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_CONTROL) preCtrol = true
            }

            override fun keyReleased(e: KeyEvent?) {
                val keyCode: Int = e?.keyCode ?: return
                if (keyCode == KeyEvent.VK_CONTROL) {
                    preCtrol = false
                    return
                }
                XApp.log("keyReleased -> $keyCode , ${e.keyChar} ,${e.extendedKeyCode} ,${e.source}")
                val key: String = tvSearch.text.trim()
                when (keyCode) {
                    KeyEvent.VK_DOWN, KeyEvent.VK_SHIFT -> showImportPop(tvSearch, adapter.datas().filter { match(key, listOf(it.title, it.content, it.tag)) }, true)
                    KeyEvent.VK_UP -> tvSearch.text = ""
                }
                for (it in adapter.datas()) {
                    it.visible = key.isEmpty() || match(key, listOf(it.title, it.content, it.tag))
                }
            }
        })
    }

    fun showImportPop(c: JComponent, datas: List<XItem>, select: Boolean) {
        val click = { i: MyMenuItem<XItem> -> i.data?.let { it.select = !it.select } ?: Unit }
        c.showPop(datas.map { m -> MyMenuItem<XItem>(m.title, m, select && m.select, click) }, Point(0, c.height + 10))
    }

    fun match(key: String, list: List<String>): Boolean {
//       val keys =  key.toLowerCase(Locale.CHINA).toCharArray()
//        for (l in list) {
//            var i = 0
//            var j = 0
//        }
        return list.find { it.contains(key) } != null
    }

    fun resetImportCount() {
        btnImport.text = "Import : ${adapter.datas().filter { it.select }.size}"
    }

    override fun getPreferredFocusedComponent(): JComponent? = tvSearch

    private fun initList() {
        val source = config.include.split(",").mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } }.toSet()
        val import = XmlHelper.parseInclude(manifest!!, source)
        val allItems = manifest.allModules().filter { it.attach == null }.map { m ->
            val item = XItem()
            item.select = import.contains(m.name)
            item.title = m.name
            item.content = "${m.path}  -- ${m.desc}"
            item.tag = m.type
            item.cbSelect.addItemListener { resetImportCount() }
            item
        }.sortedBy { it.select }
        adapter.set(allItems)
        resetImportCount()
    }
}


