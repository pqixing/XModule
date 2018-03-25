package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;

public class DpsAnalyze extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Module module = e.getData(DataKey.create("module"));
        if(module == null){
            Messages.showMessageDialog("没有选中模块","操作失败",null);
            return;
        }
        GradleUtils.runTask(project, Arrays.asList(":" + module.getName() + ":AllInnerDps"), new TaskCallback() {
            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    VirtualFile dir = project.getBaseDir().findChild(".modularization");
                    if(dir!=null) dir = dir.findChild("dependencies");
                    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true);
                    VirtualFile[] chooseFiles = FileChooser.chooseFiles(descriptor, project,dir==null? project.getBaseDir():dir);
                    FileEditorManager.getInstance(project).openFile(chooseFiles[0],true);
                });
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        }, ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);
    }
}
