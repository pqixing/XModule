package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.utils.AndroidUtils
import com.dachen.creator.utils.GradleUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
//        int exitCode = Messages.showOkCancelDialog("是否清除所有缓存", "ClearCache", null);
//        if(exitCode!=0) return
//        GradleUtils.runTask(project, ["CleanCache"])
        AndroidUtils.installApk(project,null)
    }
}
