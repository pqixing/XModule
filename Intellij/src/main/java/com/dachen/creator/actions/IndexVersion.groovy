package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class IndexVersion extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        def exitCode = Messages.showYesNoCancelDialog("确定重建版本号索引??", "版本号索引", null)

        if (exitCode != 0) return

        GradleUtils.runTask(e.getProject(), ["IndexMaven"])
    }
}
