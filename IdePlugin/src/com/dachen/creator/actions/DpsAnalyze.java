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
        if (module == null) {
            Messages.showMessageDialog("没有选中模块", "操作失败", null);
            return;
        }
        GradleUtils.addProperties(project);
        GradleUtils.addFocusInclude(project,module.getName());
        GradleUtils.runTask(project, Arrays.asList(":" + module.getName() + ":AllInnerDps"), new TaskCallback() {
            @Override
            public void onSuccess() {
                GradleUtils.clear(project);
                ApplicationManager.getApplication().invokeLater(() -> {

                    VirtualFile dir = module.getModuleFile().getParent();
                    VirtualFile target = dir;
                    dir = dir.findChild(".modularization");
                    if (dir != null) {
                        target = dir;
                        dir = dir.findChild("dependencies");
                    }
                    if(dir!=null) {
                        target = dir;
                        dir = dir.findChild("level.dp");
                    }
                    if(dir!=null) target = dir;
                    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true);
                    VirtualFile[] chooseFiles = FileChooser.chooseFiles(descriptor, project,target);
                    if (chooseFiles.length > 0) for (VirtualFile file : chooseFiles) {
                        FileEditorManager.getInstance(project).openFile(file, false);
                    }

                });
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        }, ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);
    }
}
