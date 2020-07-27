package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Contract
import org.jetbrains.annotations.Nls

class ProxyAction(val target: AnAction,val cmd:(e:AnActionEvent)->Unit) : AnAction() {
    override fun displayTextInToolbar(): Boolean {
        return target.displayTextInToolbar()
    }

    override fun useSmallerFontForTextInToolbar(): Boolean {
        return target.useSmallerFontForTextInToolbar()
    }

    override fun update(e: AnActionEvent) {
        target.update(e)
    }

    override fun beforeActionPerformedUpdate(e: AnActionEvent) {
        target.beforeActionPerformedUpdate(e)
    }

    override fun setDefaultIcon(isDefaultIconSet: Boolean) {
        target.isDefaultIcon = isDefaultIconSet
    }

    override fun isDefaultIcon(): Boolean {
        return target.isDefaultIcon
    }

    override fun setInjectedContext(worksInInjected: Boolean) {
        target.setInjectedContext(worksInInjected)
    }

    override fun isInInjectedContext(): Boolean {
        return target.isInInjectedContext
    }

    override fun isTransparentUpdate(): Boolean {
        return target.isTransparentUpdate
    }

    @Deprecated("")
    override fun startInTransaction(): Boolean {
        return target.startInTransaction()
    }

    override fun addTextOverride(place: String, text: String) {
        target.addTextOverride(place, text)
    }

    override fun applyTextOverride(e: AnActionEvent) {
        target.applyTextOverride(e)
    }

    override fun toString(): String {
        return target.toString()
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getTemplateText(): String? {
        return target.templateText
    }

    @Contract(pure = true)
    override fun isDumbAware(): Boolean {
        return target.isDumbAware
    }

    override fun actionPerformed(e: AnActionEvent) {
        cmd(e)
        target.actionPerformed(e)
    }

    companion object {
        fun getEventProject(e: AnActionEvent?): Project? {
            return AnAction.getEventProject(e)
        }
    }
}