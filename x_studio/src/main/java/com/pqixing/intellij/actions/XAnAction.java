package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pqixing.intellij.XGroup;
import org.jetbrains.annotations.NotNull;

public class XAnAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(XGroup.Companion.isBasic(e.getProject()));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
