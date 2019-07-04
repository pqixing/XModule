package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pqixing.intellij.ui.AdbTextDialog;

public class AdbText extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        AdbTextDialog dialog = new AdbTextDialog(e.getProject());
        dialog.pack();
        dialog.setVisible(true);
    }
}
