package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;

import java.util.Arrays;

public class Clone extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        int clone = Messages.showOkCancelDialog("是否Clone所有的代码，可能耗时比较长", "Clone", null);
        if (clone != 0) return;
    }
}
