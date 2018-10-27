package com.dachen.creator.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class NewModule extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showYesNoCancelDialog("暂未实现??", "创建新模块", null);
    }
}
