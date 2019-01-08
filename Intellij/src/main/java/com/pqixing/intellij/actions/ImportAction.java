package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pqixing.intellij.ui.ImportDialog;

import org.jetbrains.annotations.NotNull;


public class ImportAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new ImportDialog().show();
    }
}
