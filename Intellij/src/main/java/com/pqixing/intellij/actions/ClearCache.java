package com.pqixing.intellij.actions;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.pqixing.intellij.ui.Help;
import com.pqixing.tools.TextUtils;

;
;

public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String jsonStr = JSON.toJSONString("jsonStr");
        String title = TextUtils.INSTANCE.firstUp("this is title");
        int exitCode = Messages.showOkCancelDialog(jsonStr+","+ Help.INSTANCE.methodTest(), "title", null);
        if(exitCode!=0) return;
    }
}
