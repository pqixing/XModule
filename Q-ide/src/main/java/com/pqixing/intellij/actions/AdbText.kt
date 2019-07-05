package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.pqixing.intellij.ui.AdbTextDialog
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.TransferHandler

class AdbText : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = AdbTextDialog(e.project!!)
        dialog.pack()
        dialog.isVisible = true
    }
}
