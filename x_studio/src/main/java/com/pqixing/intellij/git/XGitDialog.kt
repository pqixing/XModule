package com.pqixing.intellij.git

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.EnvKeys
import com.pqixing.help.XmlHelper
import com.pqixing.intellij.XApp
import com.pqixing.intellij.actions.XAnAction
import com.pqixing.intellij.ui.form.XDialog
import com.pqixing.intellij.ui.form.XItem
import com.pqixing.intellij.git.uitils.GitHelper
import git4idea.GitUtil
import git4idea.commands.GitLineHandlerListener
import git4idea.repo.GitRepository
import java.awt.event.ActionListener
import java.io.File
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class XGitAction : XAnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        XGitDialog(e.project ?: return, e).show()
    }
}

class XGitDialog(val project: Project, val e: AnActionEvent) : XDialog(project) {
    private val KEY_FILE = "file"
    private val KEY_URL = "url"
    private val KEY_REPO = "repo"
    private lateinit var pTop: JPanel
    private lateinit var cbBrn: JComboBox<String>
    private lateinit var cbOp: JComboBox<IGitRun>
    private lateinit var jlPath: JLabel
    val basePath = e.project?.basePath!!
    val codeRoot = File(basePath, XmlHelper.loadConfig(basePath).codeRoot).canonicalPath
    val manifest = XmlHelper.loadManifest(basePath)!!
    val allBrns = mutableSetOf<String>()
    val listener = GitLineHandlerListener { l, k -> XApp.log(l) }

    init {
        title = "Git"
        jlPath.text = "  : $codeRoot"
        myAll = ActionListener {
            val isSelected = myCheckBoxDoNotShowDialog.isSelected
            adapter.datas().filter { it.visible }.forEach { it.select = isSelected }
        }
        XApp.post { loadItems() }
    }

    private fun loadItems() {
        val newItem = { name: String, tag: String, url: String, file: File ->
            XItem().also { item ->
                item.title = name
                item.tag = tag
                item.select = name != EnvKeys.BASIC
                item.selectAble = true
                item.params[KEY_FILE] = file
                item.params[KEY_URL] = url
                item.content = url
            }
        }
        val items = manifest.projects.map { p -> newItem(p.name, p.desc, p.url, File(codeRoot, p.path)) }.toMutableList()
        items.add(0, newItem(EnvKeys.BASIC, EnvKeys.BASIC, "Exception", File(basePath, EnvKeys.BASIC)))

        arrayOf(CheckOut(), Merge(), Clone(), Pull(), Push(), None(), Delete()).forEach { cbOp.addItem(it) }
        cbOp.addActionListener { fetchRepo() }
        fetchRepo()
        adapter.set(items)
    }

    override fun doOKAction() = XApp.runAsyn { indicator ->
        btnEnable(false)

        val gitOp = cbOp.selectedItem as IGitRun
        val selects = adapter.datas().filter { it.visible && it.select }
        if (gitOp.beforeRun(selects)) selects.onEach {
            indicator.text = "${gitOp.javaClass.simpleName} -> ${it.title} : ${it.content}"
            gitOp.run(it)
        }
        indicator.text = "${gitOp.javaClass.simpleName} -> run after"
        gitOp.afterRun(selects)

        btnEnable(true)
    }

    private fun btnEnable(enable: Boolean) {
        myCancelAction.isEnabled = enable
        myOKAction.isEnabled = enable
        cbOp.isEnabled = enable
        cbBrn.isEnabled = enable
    }


    fun fetchRepo() {
        val gitOp = cbOp.selectedItem as IGitRun
        for (item in adapter.datas()) {
            item.tag = ""
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
            val news = brs.localBranches.map { it.name }.plus(brs.remoteBranches.map { it.name.substringAfter("/") })
            news.filter { allBrns.add(it) }.forEach { cbBrn.addItem(it) }
        }
    }


    override fun createNorthPanel(): JComponent? = pTop

    fun File?.toRepo() = this?.takeIf { GitUtil.isGitRoot(it) }?.let { GitHelper.getRepo(it, project) }


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
            val clone = GitHelper.clone(project, item.get(KEY_FILE)!!, item.get(KEY_URL)!!)
            item.tag = if (clone) "Y" else "N"
        }
    }

    private inner class Push : IGitRun() {
        override fun run(item: XItem) {
            val repo = item.get<GitRepository>(KEY_REPO)
            item.tag = GitHelper.push(project, repo, listener)
        }
    }

    private inner class Pull : IGitRun() {
        override fun run(item: XItem) {
            val repo = item.get<GitRepository>(KEY_REPO)

            item.tag = GitHelper.update(project, repo, listener)
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
            item.tag = if (!GitHelper.checkBranchExists(repo, branch)) "Not Found" else GitHelper.merge(project, branch, repo, listener)
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
            item.tag = GitHelper.delete(project, branch, repo, listener)
        }
    }

    private inner class CheckOut : IGitRun() {
        override fun run(item: XItem) {
            item.tag = "O"
        }

        override fun afterRun(items: List<XItem>) {
            val branch = cbBrn.selectedItem?.toString()
            val repos = items.mapNotNull { it.get<GitRepository>(KEY_REPO) }

            var wait = true
            GitHelper.checkout(project, branch, repos) { wait = false }

            while (wait) {
            }

            repos.forEach { it.update() }

            items.forEach { item ->
                val newBranch = item.get<GitRepository>(KEY_REPO)?.currentBranchName
                if (newBranch != null) {
                    item.content = newBranch
                }
                item.tag = if (newBranch == branch) "Y" else "N"
            }
        }

    }
}

