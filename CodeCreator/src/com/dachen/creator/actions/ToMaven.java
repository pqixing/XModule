package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.codeInsight.navigation.BackgroundUpdaterTask;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.Arrays;

public class ToMaven extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        GradleUtils.runTask(project, Arrays.asList("clean"), new TaskCallback() {
            @Override
            public void onSuccess() {
                System.out.println("onSuccess-------------");
                        Messages.showInfoMessage("Haa","sdfsd");

            }

            @Override
            public void onFailure() {
                System.out.println("onFailure-------------");
            }
        });

    }
}
