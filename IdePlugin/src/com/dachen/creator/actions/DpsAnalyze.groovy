package com.dachen.creator.actions

import com.dachen.creator.GradleCallBack
import com.dachen.creator.utils.GradleUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

public class DpsAnalyze extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Module module = e.getData(LangDataKeys.MODULE)
        if (module == null) {
            Messages.showMessageDialog("没有选中模块", "操作失败", null);
            return;
        }
        GradleUtils.runTask(project, [":" + module.name + ":AllInnerDps"], new GradleCallBack() {
            @Override
            void onFinish(long time, String id, String result) {
                VirtualFile dir = module.getModuleFile().getParent();
                VirtualFile target = dir;
                dir = dir.findChild(".modularization");
                if (dir != null) {
                    target = dir;
                    dir = dir.findChild("dependencies");
                }
                if (dir != null) {
                    target = dir;
                    dir = dir.findChild("level.dp");
                }
                if (dir != null) target = dir;
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true);
                VirtualFile[] chooseFiles = FileChooser.chooseFiles(descriptor, project, target);
                if (chooseFiles.length > 0) for (VirtualFile file : chooseFiles) {
                    FileEditorManager.getInstance(project).openFile(file, false);
                }
            }
        })
    }
}
