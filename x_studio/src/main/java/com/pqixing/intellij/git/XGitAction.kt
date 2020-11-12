package com.pqixing.intellij.git

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XApp
import com.pqixing.intellij.common.XEventAction
import com.pqixing.intellij.git.uitils.GitHelper
import com.pqixing.intellij.ui.weight.*
import git4idea.GitUtil
import git4idea.commands.GitLineHandlerListener
import git4idea.repo.GitRepository
import java.awt.Point
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

open class XGitAction : XEventAction<XGitDialog>()
class XGitDialog(e: AnActionEvent) : XEventDialog(e) {
    private val KEY_FILE = "file"
    private val KEY_URL = "url"
    private val KEY_REPO = "repo"
    private val KEY_DESC = "desc"
    private lateinit var pTop: JPanel
    private lateinit var cbBrn: JComboBox<String>
    private lateinit var cbOp: JComboBox<IGitRun>
    private lateinit var jlPath: JLabel
    val codeRoot = File(basePath, XmlHelper.loadConfig(basePath).codeRoot).canonicalPath
    val manifest = XmlHelper.loadManifest(basePath)!!
    val allBrns = mutableSetOf<String>()
    var indictor: ProgressIndicator? = null
    val listener = GitLineHandlerListener { l, k ->
        XApp.log(l)
        indictor?.text = l
    }

    init {
        jlPath.text = "  : $codeRoot"
        XApp.post { loadItems() }
    }

    override fun doOnAllChange(selected: Boolean) = adapter.datas().filter { it.visible }.forEach { it.select = selected }
    override fun getTitleStr(): String = "Git"

    private fun loadItems() {
        val newItem = { name: String, tag: String, url: String, file: File ->
            XItem().also { item ->
                item.title = name
                item.tag = tag
                item.select = name != EnvKeys.BASIC
                item.selectAble = true
                item.params[KEY_FILE] = file
                item.params[KEY_URL] = url
                item.params[KEY_DESC] = tag
                item.content = url
                item.tvContent.addMouseClick(item.left) { c, e -> onContentClickR(item, url, c, e) }
            }
        }
        val items = manifest.projects.map { p -> newItem(p.name, p.desc, p.url, File(codeRoot, p.path)) }.toMutableList()
        items.add(0, newItem(EnvKeys.BASIC, EnvKeys.BASIC, "https://github.com/pqixing/XModule", File(basePath, EnvKeys.BASIC)))

        arrayOf(Check(), Merge(), Clone(), Pull(), Push(), Create(), Delete()).forEach { cbOp.addItem(it) }
        cbOp.addActionListener { fetchRepo() }
        adapter.set(items)
        XApp.invoke { fetchRepo() }
    }

    private fun onContentClickR(item: XItem, url: String, c: JComponent, e: MouseEvent) {
        val repo = item.get<GitRepository>(KEY_REPO)
        val menus = mutableListOf(MyMenuItem<Any>(url, null, false) { m -> XApp.copy(m.label) })
        if (repo != null) {
            val brs = repo.branches.let { brs -> brs.localBranches.map { it.name }.plus(brs.remoteBranches.map { it.name.substringAfter("/") }) }.toSet()
            menus += brs.sorted().map {
                MyMenuItem(it, null, false) { m ->
                    GitHelper.checkout(project, m.label, listOf(repo)) {
                        repo.update();
                        val newBranch = repo.currentBranchName
                        item.tag = item.getState(m.label == newBranch)
                        if (newBranch != null) item.content = newBranch
                    }
                }
            }
        }
        c.showPop(menus, Point(e.x, e.y))
    }

    override fun doOKAction() = XApp.runAsyn { indicator ->
        this.indictor = indicator
        btnEnable(false)

        val gitOp = cbOp.selectedItem as IGitRun
        val selects = adapter.datas().filter { it.visible && it.select }
        if (gitOp.beforeRun(selects)) selects.onEach {
            it.tag = it.getState(null)
            indicator.text = "${gitOp.javaClass.simpleName} -> ${it.title} : ${it.content}"
            kotlin.runCatching { gitOp.run(it) }
        }
        indicator.text = "${gitOp.javaClass.simpleName} -> run after"
        gitOp.afterRun(selects)

        btnEnable(true)
        XApp.invoke {
            //根据导入的CodeRoot目录,自动更改AS的版本管理
            val pVcs: ProjectLevelVcsManagerImpl = ProjectLevelVcsManagerImpl.getInstance(project) as ProjectLevelVcsManagerImpl
            pVcs.directoryMappings = adapter.datas().mapNotNull { it.get<File>(KEY_FILE) }.filter { GitUtil.isGitRoot(it) }.mapNotNull { VcsDirectoryMapping(it.absolutePath, "Git") }
            pVcs.notifyDirectoryMappingChanged()
        }
        this.indictor = null
    }

    override fun btnEnable(enable: Boolean) {
        super.btnEnable(enable)
        cbOp.isEnabled = enable
        cbBrn.isEnabled = enable
    }


    fun fetchRepo() {
        val gitOp = cbOp.selectedItem as IGitRun
        for (item in adapter.datas()) {
            item.tag = item.get<String>(KEY_DESC).toString()
            if (item.get<GitRepository>(KEY_REPO) != null) {
                item.visible = gitOp.visible(item)
                continue
            }
            val repo = item.get<File>(KEY_FILE)?.toRepo()
            item.params[KEY_REPO] = repo
            item.visible = gitOp.visible(item)
            if (repo == null) continue

            item.content = repo.currentBranchName ?: "Not Found"
            val brs = repo.branches
            val news = brs.localBranches.map { it.name }.plus(brs.remoteBranches.map { it.name.substringAfter("/") }).sorted()
            news.filter { allBrns.add(it) }.forEach { cbBrn.addItem(it) }
        }
    }


    override fun createNorthPanel(): JComponent? = pTop

    fun File?.toRepo() = this?.takeIf { GitUtil.isGitRoot(it) }?.let { GitHelper.getRepo(it, project) }


    /**  start run **/
    abstract inner class IGitRun {
        open fun visible(item: XItem): Boolean = item.get<GitRepository>(KEY_REPO) != null
        open fun run(item: XItem) {}
        open fun afterRun(items: List<XItem>) {}
        open fun beforeRun(items: List<XItem>): Boolean = true
        override fun toString(): String = this.javaClass.simpleName
    }

    private inner class None : IGitRun() {
        override fun visible(item: XItem) = true
    }

    private inner class Clone : IGitRun() {
        override fun visible(item: XItem): Boolean = item.get<String>(KEY_URL) != null && !super.visible(item)
        override fun run(item: XItem) {
            val file = item.get<File>(KEY_FILE)
            if (file != null && GitUtil.isGitRoot(file)) {
                item.tag = item.getState(true)
            } else {
                item.tag = item.getState(GitHelper.clone(project, item.get(KEY_FILE)!!, item.get(KEY_URL)!!, listener))
            }
        }
    }

    private inner class Push : IGitRun() {
        override fun run(item: XItem) {
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = GitHelper.push(project, repo, listener).let { item.getState(it == "Success", it) }
        }
    }

    private inner class Pull : IGitRun() {
        override fun run(item: XItem) {
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = GitHelper.update(project, repo, listener).let { item.getState(it == "Success", it) }
        }
    }

    private inner class Merge : IGitRun() {
        override fun beforeRun(items: List<XItem>): Boolean {
            var enable = false
            XApp.invoke(true) {
                enable = Messages.OK == Messages.showOkCancelDialog(project, "Make Sure Merge ${cbBrn.selectedItem?.toString()}", "Warning", "Delete", "Cancel", null)
            }
            return enable
        }

        override fun run(item: XItem) {
            val branch = cbBrn.selectedItem?.toString()
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = (if (!GitHelper.checkBranchExists(repo, branch)) "None" else GitHelper.merge(project, branch, repo, listener)).let { item.getState(it == "Success", it) }
        }
    }

    private inner class Delete : IGitRun() {
        override fun beforeRun(items: List<XItem>): Boolean {
            var enable = false
            XApp.invoke(true) {
                enable = Messages.OK == Messages.showOkCancelDialog(project, "Make Sure Delete ${cbBrn.selectedItem?.toString()}", "Warning", "Delete", "Cancel", null)
            }
            return enable
        }

        override fun run(item: XItem) {

            val branch = cbBrn.selectedItem?.toString() ?: return
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = GitHelper.delete(project, branch, repo, listener).let { item.getState(it == "Success", it) }
        }
    }

    private inner class Create : IGitRun() {
        override fun run(item: XItem) {
            val branch = cbBrn.selectedItem?.toString() ?: return
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = GitHelper.create(project, branch, repo, listener).let { item.getState(it == "Success", it) }

        }
    }

    private inner class Check : IGitRun() {
        override fun run(item: XItem) {
            item.tag = item.getState(null)
        }

        override fun afterRun(items: List<XItem>) {
            val branch = cbBrn.selectedItem?.toString()
            val repos = items.mapNotNull { it.get<GitRepository>(KEY_REPO) }

            var wait = true
            val end = System.currentTimeMillis() + 15000
            GitHelper.checkout(project, branch, repos) { wait = false }

            while (wait && System.currentTimeMillis() < end) {
            }

            repos.forEach { f -> f.update() }

            items.forEach { item ->
                val newBranch = item.get<GitRepository>(KEY_REPO)?.currentBranchName
                if (newBranch != null) {
                    item.content = newBranch
                }
                item.tag = item.getState(newBranch == branch)
            }
        }

    }

    private fun XItem.getState(success: Boolean?, newTag: String? = null): String = XItem.state(success) + (newTag
            ?: this.get<String>(KEY_DESC).toString())
}

