package com.dachen.creator.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenProperties extends OpenFileAction {

    @Override
    VirtualFile[] getFiles(Project project) {
        return new VirtualFile[]{project.getBaseDir().findChild("modularization.properties")};
    }

    @Override
    protected String getPath(int pos) {
        return "modularization.properties";
    }
}
