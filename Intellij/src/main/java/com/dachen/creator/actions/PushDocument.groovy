package com.dachen.creator.actions

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class PushDocument extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        def exitCode = Messages.showYesNoCancelDialog("确定提交Document工程??", "提交Document", null)

        if (exitCode != 0) return

        GradleUtils.runTask(e.getProject(),["PushDocument"])
    }
}
