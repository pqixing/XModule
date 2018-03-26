package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.project.Project;

import java.util.Arrays;

public class Update extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        GradleUtils.addProperties(project);
        GradleUtils.addFocusInclude(project,"empty");
        GradleUtils.runTask(project, Arrays.asList("GitUpdate"), new TaskCallback() {
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
