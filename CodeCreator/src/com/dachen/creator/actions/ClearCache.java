package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.Arrays;

public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        int exitCode = Messages.showOkCancelDialog("是否清除所有缓存", "ClearCache", null);
        if(exitCode!=0) return;
        GradleUtils.addProperties(project);
        GradleUtils.addFocusInclude(project, "empty");
        GradleUtils.runTask(project, Arrays.asList("CleanCache"), new TaskCallback() {
            @Override
            public void onSuccess() {
                GradleUtils.clear(project);
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        });

    }
}
